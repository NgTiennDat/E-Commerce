package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.model.entity.Permission;
import com.eCommerce.auth.repository.PermissionRepository;
import com.eCommerce.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean hasPermission(String username, String httpMethod, String requestPath) {
        // Chuẩn hóa method (GET/POST/...)
        String method = httpMethod == null ? "" : httpMethod.toUpperCase();

        // 1. Lấy tất cả permission path của user theo method
        List<Permission> permissions =
                permissionRepository.findAllByUsernameAndMethod(username, method);

        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        // 2. Chuẩn hóa path thực tế
        String path = normalize(requestPath);

        // 3. So khớp pattern trong DB với path thực tế
        for (Permission p : permissions) {
            String templatePath = normalize(p.getPath()); // path trong DB
            if (antPathMatcher.match(templatePath, path)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String path) {
        if (path == null || path.isBlank()) return "/";

        // bỏ query string nếu có ?page=..., ?size=...
        int qIndex = path.indexOf("?");
        if (qIndex != -1) {
            path = path.substring(0, qIndex);
        }

        String normalized = path.replaceAll("/+", "/"); // chống // hoặc ///
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
