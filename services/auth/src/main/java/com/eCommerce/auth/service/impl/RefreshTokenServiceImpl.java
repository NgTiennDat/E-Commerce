package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.model.entity.RefreshToken;
import com.eCommerce.auth.model.entity.Role;
import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.repository.RefreshTokenRepository;
import com.eCommerce.auth.service.RedisService;
import com.eCommerce.auth.service.RefreshTokenService;
import com.eCommerce.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
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

        // 1) build JWT refresh token (dùng JwtUtils)
        String token = jwtUtils.generateToken(
                user.getUsername(),
                roles,
                refreshExpiryMillis
        );

        long expiryTime = System.currentTimeMillis() + refreshExpiryMillis;

        // 2) lưu DB
        RefreshToken entity = RefreshToken.builder()
                .token(token)
                .username(user.getUsername())
                .expiryTime(expiryTime)
                .revoked(false)
                .expired(false)
                .build();
        refreshTokenRepository.save(entity);

        // 3) lưu Redis
        long ttlSeconds = refreshExpiryMillis / 1000;
        redisService.set(RT_BY_TOKEN + token, user.getUsername(), ttlSeconds);

        return token;
    }

    public boolean validateRefreshToken(String token) {
        // 1. check blacklist
        if (redisService.exists(RT_BLACKLIST + token)) {
            return false;
        }

        // 2. check Redis
        String username = redisService.get(RT_BY_TOKEN + token);
        if (username != null) {
            // optional: verify JWT signature, exp bằng JwtUtils
            return jwtUtils.isTokenValid(token);
        }

        // 3. fallback DB
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiryTime() > System.currentTimeMillis())
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

        redisService.set(RT_BLACKLIST + token, "revoked", ttl);
        redisService.delete(RT_BY_TOKEN + token);
    }
}
