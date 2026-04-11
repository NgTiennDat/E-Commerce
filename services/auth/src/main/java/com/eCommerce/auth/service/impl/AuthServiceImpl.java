package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.common.AuthConstants;
import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.model.request.LoginRequest;
import com.eCommerce.auth.model.request.RefreshTokenRequest;
import com.eCommerce.auth.model.response.AuthResponse;
import com.eCommerce.auth.model.response.UserResponse;
import com.eCommerce.auth.repository.UserRepository;
import com.eCommerce.auth.service.AuthService;
import com.eCommerce.auth.service.RateLimiterService;
import com.eCommerce.auth.service.RefreshTokenService;
import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.common.redis.service.RedisService;
import com.eCommerce.common.security.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisService redisService;
    private final RateLimiterService rateLimiterService;
    private final RefreshTokenService refreshTokenService;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Value("${application.security.jwt.expiration}")
    private Long accessTokenExpiration;

    @Override
    public AuthResponse doLogin(LoginRequest loginRequest,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        String ip = Optional.ofNullable(request.getRemoteAddr()).orElse("unknown");
        rateLimiterService.assertAllowed("login:" + ip);

        try {
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                    .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
            }

            return buildAuthResponse(response, user);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request,
                                     HttpServletRequest httpRequest,
                                     HttpServletResponse response) {
        String incoming = request.getRefreshToken();
        if (incoming == null || incoming.isBlank()) {
            throw new CustomException(ResponseCode.INVALID_REQUEST);
        }

        String ip = Optional.ofNullable(httpRequest.getRemoteAddr()).orElse("unknown");
        rateLimiterService.assertAllowed("refresh:" + ip);

        // Bước 1: Kiểm tra đúng loại token — fail fast
        try {
            if (!jwtUtils.isRefreshToken(incoming)) {
                throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception ex) {
            throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
        }

        // Bước 2: Validate token qua RefreshTokenService (blacklist + Redis + DB)
        if (!refreshTokenService.validate(incoming)) {
            throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
        }

        // Bước 3: Extract username và load user
        String username;
        try {
            username = jwtUtils.extractUsername(incoming);
        } catch (Exception ex) {
            throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

        // Bước 4: Revoke token cũ (token rotation — mỗi refresh tạo token mới)
        refreshTokenService.revoke(incoming);

        return buildAuthResponse(response, user);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Blacklist access token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(AuthConstants.BEARER_PREFIX.length());
            long ttl = accessTokenExpiration != null ? accessTokenExpiration : 3600_000L;
            redisService.setValue(
                    AuthConstants.TOKEN_BLACKLIST + token,
                    "revoked",
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }

        // Revoke refresh token
        String refreshToken = extractRefreshCookie(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                refreshTokenService.revoke(refreshToken);
            } catch (Exception ignored) {
                // Token đã hết hạn hoặc không tồn tại — logout vẫn tiếp tục
            }
        }

        // Xóa cookie
        Cookie cookie = new Cookie(AuthConstants.REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private AuthResponse buildAuthResponse(HttpServletResponse response, User user) {
        String newAccess  = jwtUtils.generateAccessToken(user);
        String newRefresh = refreshTokenService.create(user);  // delegate hoàn toàn
        response.addCookie(buildRefreshCookie(newRefresh));

        return AuthResponse.builder()
                .user(toUserResponse(user))
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .build();
    }

    private Cookie buildRefreshCookie(String refreshToken) {
        Cookie cookie = new Cookie(AuthConstants.REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        return cookie;
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (AuthConstants.REFRESH_TOKEN_COOKIE.equals(c.getName())) {
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
