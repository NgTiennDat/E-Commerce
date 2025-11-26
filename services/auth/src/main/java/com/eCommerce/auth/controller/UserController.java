package com.eCommerce.auth.controller;

import com.eCommerce.auth.model.request.RegistrationRequest;
import com.eCommerce.auth.service.UserService;
import com.eCommerce.common.payload.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegistrationRequest request
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(userService.register(request)));
    }
}
