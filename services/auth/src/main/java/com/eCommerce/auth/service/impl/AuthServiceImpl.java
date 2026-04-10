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

        // Bước 1: Kiểm tra type trước khi parse username.
        // Nếu client gửi nhầm access token vào đây, từ chối ngay.
        // Không để logic xuống dưới mới phát hiện — fail fast.
        try {
            if (!jwtUtils.isRefreshToken(incoming)) {
                throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception ex) {
            // Token bị tamper hoặc hết hạn — không lộ chi tiết lỗi ra ngoài
            throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
        }

        // Bước 2: Extract username sau khi đã xác nhận đúng loại token
        String username;
        try {
            username = jwtUtils.extractUsername(incoming);
        } catch (Exception ex) {
            throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

        // Bước 3: Kiểm tra token có khớp với cái đang lưu trong Redis không
        String redisKey = AuthConstants.REFRESH_TOKEN_BY_USER + user.getId();
        Object cached = redisService.getValue(redisKey);
        if (cached == null || !incoming.equals(cached.toString())) {
            throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
        }

        return getAuthResponse(response, user);
    }

    private AuthResponse getAuthResponse(HttpServletResponse response, User user) {
        // Hai method riêng biệt — khác nhau về expiry và claims
        String newAccess  = jwtUtils.generateAccessToken(user);   // 15 phút, có roles
        String newRefresh = jwtUtils.generateRefreshToken(user);  // 7 ngày, chỉ có type=refresh
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
        if (authHeader != null && authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(AuthConstants.BEARER_PREFIX.length());
            long ttl = accessTokenExpiration != null ? accessTokenExpiration : 3600_000L;
            redisService.setValue(AuthConstants.TOKEN_BLACKLIST + token, "revoked", ttl, TimeUnit.MILLISECONDS);
        }

        String refreshToken = extractRefreshCookie(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                String username = jwtUtils.extractUsername(refreshToken);
                userRepository.findByUsername(username).ifPresent(u ->
                        redisService.delete(AuthConstants.REFRESH_TOKEN_BY_USER + u.getId()));
            } catch (Exception ignored) {
                // ignore invalid refresh on logout
            }
        }

        Cookie cookie = new Cookie(AuthConstants.REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void cacheRefreshToken(User user, String refreshToken) {
        String refreshTokenKey = AuthConstants.REFRESH_TOKEN_BY_USER + user.getId();
        redisService.setValue(refreshTokenKey, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);
        logger.info("Refresh token cached for userId={}", user.getId());
    }

    private Cookie buildRefreshCookie(String refreshToken) {
        Cookie refreshTokenCookies = new Cookie(AuthConstants.REFRESH_TOKEN_COOKIE, refreshToken);
        refreshTokenCookies.setHttpOnly(true);
        refreshTokenCookies.setSecure(true);
        refreshTokenCookies.setPath("/");
        refreshTokenCookies.setMaxAge((int) (refreshTokenExpiration / 1000));
        return refreshTokenCookies;
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
