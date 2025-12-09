package com.eCommerce.gateway.security;

import org.springframework.util.AntPathMatcher;

import java.util.List;

public final class PublicRoutes {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> ROUTES = List.of(
            "/api/v1/auth/**",
            "/api/v1/user/register",
            "/api/v1/products",
            "/api/v1/products/*/related",
            "/actuator/**",
            "/eureka/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    private PublicRoutes() {
    }

    public static boolean isPublic(String path) {
        return ROUTES.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    public static String[] routesArray() {
        return ROUTES.toArray(new String[0]);
    }
}
