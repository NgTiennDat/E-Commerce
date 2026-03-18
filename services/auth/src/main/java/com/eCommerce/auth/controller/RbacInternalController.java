package com.eCommerce.auth.controller;

import com.eCommerce.auth.model.request.RbacCheckRequest;
import com.eCommerce.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/rbac")
@RequiredArgsConstructor
public class RbacInternalController {

    private final PermissionService permissionService;

    @PostMapping("/check")
    public ResponseEntity<Boolean> checkPermission(@RequestBody RbacCheckRequest req) {
        boolean result = permissionService.hasPermission(req.getUsername(), req.getMethod(), req.getPath());
        return ResponseEntity.ok(result);
    }
}
