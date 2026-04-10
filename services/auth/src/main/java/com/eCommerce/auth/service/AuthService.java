package com.eCommerce.auth.service;

import com.eCommerce.auth.model.request.LoginRequest;
import com.eCommerce.auth.model.request.RefreshTokenRequest;
import com.eCommerce.auth.model.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse doLogin(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);

    AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

}
