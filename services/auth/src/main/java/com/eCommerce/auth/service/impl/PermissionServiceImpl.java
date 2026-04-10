package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.model.entity.Permission;
import com.eCommerce.auth.repository.PermissionRepository;
import com.eCommerce.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    // Tên cache — khớp với cache name trong @Cacheable bên dưới
    // Spring Cache sẽ tạo Redis key: "rbac::{cacheKey}"
    static final String CACHE_NAME = "rbac";

    private final PermissionRepository permissionRepository;
    private final CacheManager cacheManager;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * Kiểm tra user có quyền thực hiện httpMethod trên requestPath không.
     *
     * Cache strategy:
     *   Key   = "{username}:{method}" — 1 entry cover tất cả path của user+method
     *   Value = List<Permission> — fetch 1 lần, match path in-memory
     *   TTL   = 60s (cấu hình trong application.yml)
     *
     * Tại sao key là username:method, không phải username:method:path?
     * Vì method này fetch toàn bộ permission theo username+method rồi mới match path.
     * Cache theo username:method → 1 DB query cover mọi path của user đó.
     * Cache theo username:method:path → mỗi path là 1 DB query riêng — kém hiệu quả hơn.
     */
    @Override
    @Cacheable(
            value  = CACHE_NAME,
            key    = "#username + ':' + #httpMethod",
            unless = "#result == null"
    )
    public boolean hasPermission(String username, String httpMethod, String requestPath) {
        String method = httpMethod == null ? "" : httpMethod.toUpperCase();
        String path   = normalize(requestPath);

        List<Permission> permissions =
                permissionRepository.findAllByUsernameAndMethod(username, method);

        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        for (Permission p : permissions) {
            if (antPathMatcher.match(normalize(p.getPath()), path)) {
                log.debug("RBAC match: user={} {}:{} → permission={}",
                        username, method, path, p.getCode());
                return true;
            }
        }
        return false;
    }

    /**
     * Xóa toàn bộ cache permission của 1 user (tất cả HTTP method).
     *
     * Tại sao không dùng @CacheEvict?
     * @CacheEvict chỉ xóa được 1 key cụ thể hoặc toàn bộ cache (allEntries=true).
     * Không hỗ trợ xóa theo pattern (ví dụ: xóa tất cả key có prefix "user1:").
     * allEntries=true xóa cache của tất cả user — quá rộng, gây cache miss hàng loạt.
     *
     * Giải pháp: inject CacheManager, lấy cache "rbac", xóa từng key theo từng method.
     * HTTP methods cần cover: GET, POST, PUT, PATCH, DELETE.
     */
    @Override
    public void evictUserPermissionCache(String username) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            return;
        }
        // Xóa cache cho tất cả HTTP method có thể của user này
        for (String method : List.of("GET", "POST", "PUT", "PATCH", "DELETE")) {
            cache.evict(username + ":" + method);
        }
        log.info("Evicted RBAC cache for user={}", username);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String normalize(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        path = path.trim();

        int qIndex = path.indexOf("?");
        if (qIndex != -1) {
            path = path.substring(0, qIndex);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        String normalized = path.replaceAll("/+", "/");

        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
