package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.common.AuthConstants;
import com.eCommerce.auth.model.entity.RefreshToken;
import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.repository.RefreshTokenRepository;
import com.eCommerce.auth.service.RefreshTokenService;
import com.eCommerce.common.redis.service.RedisService;
import com.eCommerce.common.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisService redisService;
    private final JwtUtils jwtUtils;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiryMillis;

    /**
     * Tạo refresh token mới:
     *   1. Generate JWT refresh token
     *   2. Lưu vào DB với expiredAt đúng
     *   3. Index vào Redis để validate nhanh
     *
     * Tại sao lưu cả DB lẫn Redis?
     *   DB  → source of truth, audit trail, hỗ trợ revokeAll()
     *   Redis → fast lookup khi validate (tránh DB query mỗi request)
     */
    @Override
    @Transactional
    public String create(User user) {
        String token = jwtUtils.generateRefreshToken(user);

        LocalDateTime expiredAt = LocalDateTime.now()
                .plusSeconds(refreshExpiryMillis / 1000);

        // Lưu DB
        RefreshToken entity = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiredAt(expiredAt)
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(entity);

        // Index Redis — key: token string → value: username
        long ttlSeconds = refreshExpiryMillis / 1000;
        redisService.setValue(
                AuthConstants.REFRESH_TOKEN_BY_VALUE + token,
                user.getUsername(),
                ttlSeconds,
                TimeUnit.SECONDS
        );

        log.debug("Refresh token created for userId={}", user.getId());
        return token;
    }

    /**
     * Validate refresh token theo thứ tự:
     *   1. Blacklist check — nếu đã revoke, từ chối ngay
     *   2. Redis check — fast path, không cần DB
     *   3. DB fallback — khi Redis miss (TTL hết hoặc Redis restart)
     *
     * Tại sao check blacklist trước?
     * Blacklist là "hard no" — dù Redis hay DB có token, nếu đã blacklist thì invalid.
     * Check blacklist trước tránh false positive.
     */
    @Override
    public boolean validate(String token) {
        // 1. Blacklist check
        if (redisService.exists(AuthConstants.REFRESH_TOKEN_BLACKLIST + token)) {
            log.debug("Refresh token is blacklisted");
            return false;
        }

        // 2. Redis fast path
        Object cached = redisService.getValue(AuthConstants.REFRESH_TOKEN_BY_VALUE + token);
        if (cached != null) {
            // Redis còn TTL → token chưa hết hạn, verify JWT signature thêm
            return jwtUtils.isTokenValid(token);
        }

        // 3. DB fallback — Redis miss (restart hoặc TTL hết trước DB)
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiredAt() != null
                        && rt.getExpiredAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    /**
     * Revoke 1 token cụ thể (logout 1 thiết bị):
     *   1. DB: đánh dấu is_revoked = true
     *   2. Redis: xóa index key
     *   3. Redis: thêm vào blacklist với TTL còn lại
     *
     * Tại sao cần blacklist nếu đã xóa Redis index?
     * Nếu chỉ xóa index, token vẫn pass JWT signature check.
     * Blacklist đảm bảo token bị từ chối ngay cả khi Redis index bị miss.
     */
    @Override
    @Transactional
    public void revoke(String token) {
        // DB revoke
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });

        // Lấy TTL còn lại để set cho blacklist entry
        Long remainingTtl = redisService.getTtl(
                AuthConstants.REFRESH_TOKEN_BY_VALUE + token
        );
        long ttl = (remainingTtl != null && remainingTtl > 0)
                ? remainingTtl
                : refreshExpiryMillis / 1000; // fallback: dùng full TTL

        // Xóa index, thêm blacklist
        redisService.delete(AuthConstants.REFRESH_TOKEN_BY_VALUE + token);
        redisService.setValue(
                AuthConstants.REFRESH_TOKEN_BLACKLIST + token,
                "revoked",
                ttl,
                TimeUnit.SECONDS
        );

        log.debug("Refresh token revoked");
    }

    /**
     * Revoke tất cả token của user (đổi password / force logout).
     * Dùng bulk UPDATE trong DB thay vì load từng entity — hiệu quả hơn.
     *
     * Lưu ý: không thể xóa Redis index của tất cả token vì không biết
     * token string nào đang tồn tại trong Redis (không scan key).
     * TTL tự nhiên sẽ dọn dẹp Redis sau refreshExpiryMillis.
     * Nếu cần immediate effect, cần lưu thêm set "user:{id}:tokens" trong Redis.
     */
    @Override
    @Transactional
    public void revokeAll(Long userId) {
        // Lấy danh sách token còn active để blacklist trong Redis
        List<RefreshToken> activeTokens =
                refreshTokenRepository.findActiveByUserId(userId);

        // Bulk revoke trong DB
        refreshTokenRepository.revokeAllByUserId(userId);

        // Blacklist từng token trong Redis
        long ttl = refreshExpiryMillis / 1000;
        for (RefreshToken rt : activeTokens) {
            redisService.delete(
                    AuthConstants.REFRESH_TOKEN_BY_VALUE + rt.getToken()
            );
            redisService.setValue(
                    AuthConstants.REFRESH_TOKEN_BLACKLIST + rt.getToken(),
                    "revoked",
                    ttl,
                    TimeUnit.SECONDS
            );
        }

        log.info("Revoked all refresh tokens for userId={}, count={}",
                userId, activeTokens.size());
    }
}
