package com.eCommerce.auth.controller;

import com.eCommerce.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final PermissionService permissionService;

    @GetMapping("/has-permission")
    public ResponseEntity<Boolean> hasPermission(
            @RequestParam String username,
            @RequestParam String method,
            @RequestParam String path
    ) {
        boolean result = permissionService.hasPermission(username, method, path);
        return ResponseEntity.ok(result);
    }
}
