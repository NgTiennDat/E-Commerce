package com.eCommerce.gateway.security;

import java.util.List;

public final class SecurityWhiteList {

    /**
     * Các path public (không cần JWT, không cần RBAC) dùng cho SecurityConfig.
     * Nếu sau này muốn thêm/bớt chỉ sửa TẠI ĐÂY.
     */
    public static final String[] PUBLIC_PATHS = {
            "/actuator/**",
            "/eureka/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",

            "/api/v1/auth/**",          // login, refresh, logout...
            "/api/v1/users/register",   // register
            "/api/v1/products",         // list products
            "/api/v1/products/*/related"
    };

    /**
     * Dùng cho GlobalFilter (AntPathMatcher.match)
     */
    public static final List<String> PUBLIC_PATTERNS = List.of(PUBLIC_PATHS);

    private SecurityWhiteList() {
    }
}
