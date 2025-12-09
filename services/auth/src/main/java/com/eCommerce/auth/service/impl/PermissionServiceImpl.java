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
        String method = httpMethod == null ? "" : httpMethod.toUpperCase();

        // 1. Lấy tất cả permissions theo user + method
        List<Permission> permissions =
                permissionRepository.findAllByUsernameAndMethod(username, method);

        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        // 2. Chuẩn hoá request path
        String path = normalize(requestPath);

        // 3. Match path thực tế với pattern trong DB
        for (Permission p : permissions) {
            String templatePath = normalize(p.getPath());
            if (antPathMatcher.match(templatePath, path)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String path) {
        if (path == null || path.isBlank()) return "/";

        int qIndex = path.indexOf("?");
        if (qIndex != -1) {
            path = path.substring(0, qIndex);
        }

        String normalized = path.replaceAll("/+", "/");
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
