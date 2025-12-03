package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.model.request.LoginRequest;
import com.eCommerce.auth.model.response.AuthResponse;
import com.eCommerce.auth.model.response.UserResponse;
import com.eCommerce.auth.repository.UserRepository;
import com.eCommerce.auth.service.AuthService;
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
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private final RedisService redisService;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    /**
     * Performs user login by validating credentials and generating JWT tokens.
     *
     * @param loginRequest the login request containing username/email and password
     * @param request      the HTTP servlet request
     * @param response     the HTTP servlet response
     * @return an AuthResponse containing user details and JWT tokens
     */
    @Override
    public AuthResponse doLogin(
            LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response
    ) {
        try {

            var user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                    .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new CustomException(ResponseCode.INVALID_CREDENTIALS);
            }

            // 3. Tạo JWT token (AccessToken và RefreshToken)
            String accessToken = jwtUtils.generateToken(user);
            String refreshToken = jwtUtils.generateToken(user);

            // 4. Lưu refresh token vào Redis với TTL
            String refreshTokenKey = "refresh_token_" + user.getId();
            redisService.setValue(refreshTokenKey, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);

            logger.info("Refresh Token: {}    RefreshToken type: {}", refreshToken, ((Object) refreshToken).getClass().getName());

            var refreshTokenCookies = new Cookie("refreshToken", refreshToken);
            refreshTokenCookies.setHttpOnly(true);
            refreshTokenCookies.setSecure(true);
            refreshTokenCookies.setPath("/");
            refreshTokenCookies.setMaxAge((int) (refreshTokenExpiration / 1000));
            response.addCookie(refreshTokenCookies);

            var userResponse = UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            return AuthResponse.builder()
                    .user(userResponse)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .build();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error while registering user: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void doLogout() {

    }
}
