package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.model.entity.RefreshToken;
import com.eCommerce.auth.model.entity.Role;
import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.repository.RefreshTokenRepository;
import com.eCommerce.auth.service.RefreshTokenService;
import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.common.redis.service.RedisService;
import com.eCommerce.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final Logger logger = LogManager.getLogger(RefreshTokenServiceImpl.class);

    private static final String REFRESH_TOKEN_PREFIX = "REDIS_KEY:";
    private final RedisTemplate<String, Object> redisTemplate;

    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisService redisService;
    private final JwtUtils jwtUtils;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiryMillis;

    private static final String RT_BY_TOKEN = "refresh_token:token:";
    private static final String RT_BLACKLIST = "refresh_token:blacklist:";

    public String createRefreshToken(User user) {
        // roles để put vào JWT
        List<String> roles = user.getRoles()
                .stream().map(Role::getCode).toList();

//         1) build JWT refresh token (dùng JwtUtils)
        String token = jwtUtils.generateToken(user);

        long expiryTime = System.currentTimeMillis() + refreshExpiryMillis;

        // 2) lưu DB
        RefreshToken entity = RefreshToken.builder()
                .token(token)
                .user(user)
        .build();
        refreshTokenRepository.save(entity);

        // 3) lưu Redis
        long ttlSeconds = refreshExpiryMillis / 1000;
        redisService.setValue(RT_BY_TOKEN + token, user.getUsername(), ttlSeconds, TimeUnit.SECONDS);

        return token;
    }

    public boolean validateRefreshToken(String token) {
        // 1. check blacklist
        if (redisService.exists(RT_BLACKLIST + token)) {
            return false;
        }

        // 2. check Redis
        String username = redisService.getValue(RT_BY_TOKEN + token).toString();
        if (username != null) {
            // optional: verify JWT signature, exp bằng JwtUtils
            return jwtUtils.isTokenValid(token);
        }

        // 3. fallback DB
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked())
                .isPresent();
    }

    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });

        Long ttl = redisService.getTtl(RT_BY_TOKEN + token);
        if (ttl == null || ttl <= 0) {
            ttl = 3600L; // fallback 1h
        }

        redisService.setValue(RT_BLACKLIST + token, "revoked", ttl, TimeUnit.SECONDS);
        redisService.delete(RT_BY_TOKEN + token);
    }


    @Override
    public void cacheRefreshToken(RefreshToken refreshToken) {
        try {
            String key = REFRESH_TOKEN_PREFIX + refreshToken.getId();
            long ttl = refreshToken.getExpiredAt().toEpochSecond(java.time.ZoneOffset.UTC)
                    - java.time.Instant.now().getEpochSecond();

            redisTemplate.opsForValue().set(key, refreshToken, ttl, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            throw new CustomException(ResponseCode.CACHE_FAILED);
        }
    }

    /**
     * Retrieves a cached RefreshToken by its ID.
     *
     * @param refreshTokenId the ID of the RefreshToken to retrieve
     * @return the cached RefreshToken, or null if not found
     */
    @Override
    public RefreshToken getCacheRefreshToken(String refreshTokenId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + refreshTokenId;
            Object cachedValue = redisTemplate.opsForValue().get(key);
            return cachedValue instanceof RefreshToken ? (RefreshToken) cachedValue : null;
        }  catch (RuntimeException e) {
            logger.error(e.getMessage());
            throw new CustomException(ResponseCode.CACHE_FAILED);
        }
    }

    /**
     * Delete a cached RefreshToken by its IDs
     * @param refreshTokenId the ID of the RefreshToken to retrieve
     */
    @Override
    public void deleteCacheToken(String refreshTokenId) {
        try {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshTokenId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new CustomException(ResponseCode.CACHE_FAILED);
        }
    }
}
