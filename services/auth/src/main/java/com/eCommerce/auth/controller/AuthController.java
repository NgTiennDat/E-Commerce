package com.eCommerce.auth.controller;

import com.eCommerce.auth.model.request.RegistrationRequest;
import com.eCommerce.auth.service.AuthService;
import com.eCommerce.common.payload.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/v1/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegistrationRequest request
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(authService.register(request)));
    }

    @PostMapping("/doLogin")
    public String doLogin() {
        return "Login successful";
    }
}
