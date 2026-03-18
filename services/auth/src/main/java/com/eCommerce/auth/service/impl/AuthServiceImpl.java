package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.model.request.LoginRequest;
import com.eCommerce.auth.model.request.RefreshTokenRequest;
import com.eCommerce.auth.model.response.AuthResponse;
import com.eCommerce.auth.model.response.UserResponse;
import com.eCommerce.auth.repository.UserRepository;
import com.eCommerce.auth.service.AuthService;
import com.eCommerce.auth.service.RateLimiterService;
import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.common.redis.service.RedisService;
import com.eCommerce.common.security.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);
    private static final String REFRESH_PREFIX = "refresh_token_";
    private static final String BLACKLIST_PREFIX = "BlackList:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisService redisService;
    private final RateLimiterService rateLimiterService;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Value("${application.security.jwt.expiration}")
    private Long accessTokenExpiration;

    @Override
    public AuthResponse doLogin(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        // Simple per-IP limiter to slow brute-force; for production use gateway/distributed limiter.
        String ip = Optional.ofNullable(request.getRemoteAddr()).orElse("unknown");
        rateLimiterService.assertAllowed("login:" + ip);

        try {
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                    .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
            }

            return getAuthResponse(response, user);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        String incoming = request.getRefreshToken();
        if (incoming == null || incoming.isBlank()) {
            throw new CustomException(ResponseCode.INVALID_REQUEST);
        }

        String ip = Optional.ofNullable(httpRequest.getRemoteAddr()).orElse("unknown");
        rateLimiterService.assertAllowed("refresh:" + ip);

        String username;
        try {
            username = jwtUtils.extractUsername(incoming);
        } catch (Exception ex) {
            throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

        String redisKey = REFRESH_PREFIX + user.getId();
        Object cached = redisService.getValue(redisKey);
        if (cached == null || !incoming.equals(cached.toString())) {
            throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
        }

        return getAuthResponse(response, user);
    }

    private AuthResponse getAuthResponse(HttpServletResponse response, User user) {
        String newAccess = jwtUtils.generateToken(user);
        String newRefresh = jwtUtils.generateToken(user);
        cacheRefreshToken(user, newRefresh);
        response.addCookie(buildRefreshCookie(newRefresh));

        return AuthResponse.builder()
                .user(toUserResponse(user))
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length());
            long ttl = accessTokenExpiration != null ? accessTokenExpiration : 3600_000L;
            redisService.setValue(BLACKLIST_PREFIX + token, "revoked", ttl, TimeUnit.MILLISECONDS);
        }

        String refreshToken = extractRefreshCookie(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                String username = jwtUtils.extractUsername(refreshToken);
                userRepository.findByUsername(username).ifPresent(u ->
                        redisService.delete(REFRESH_PREFIX + u.getId()));
            } catch (Exception ignored) {
                // ignore invalid refresh on logout
            }
        }

        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void cacheRefreshToken(User user, String refreshToken) {
        String refreshTokenKey = REFRESH_PREFIX + user.getId();
        redisService.setValue(refreshTokenKey, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);
        logger.info("Refresh token cached for userId={}", user.getId());
    }

    private Cookie buildRefreshCookie(String refreshToken) {
        Cookie refreshTokenCookies = new Cookie("refreshToken", refreshToken);
        refreshTokenCookies.setHttpOnly(true);
        refreshTokenCookies.setSecure(true);
        refreshTokenCookies.setPath("/");
        refreshTokenCookies.setMaxAge((int) (refreshTokenExpiration / 1000));
        return refreshTokenCookies;
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if ("refreshToken".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(true)
                .createdAt(user.getCreatedAt() == null ? LocalDateTime.now() : user.getCreatedAt())
                .updatedAt(user.getUpdatedAt() == null ? LocalDateTime.now() : user.getUpdatedAt())
                .build();
    }
}
