package com.eCommerce.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RequiredArgsConstructor
@RestController
public class AuthController {

    @PostMapping("/doLogin")
    public String doLogin() {
        return "Login successful";
    }
}
