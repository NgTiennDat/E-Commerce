package com.eCommerce.gateway.security;

public final class SecurityWhiteList {

    private SecurityWhiteList() {}

    public static final String[] PUBLIC_PATHS = {
            "/actuator/**",
            "/eureka/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",

            "/api/v1/auth/**",
            "/api/v1/users/register",
            "/api/v1/products",
            "/api/v1/products/*/related",
            "/api/v1/categories/**"
    };
}
