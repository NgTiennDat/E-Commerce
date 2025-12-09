package com.eCommerce.gateway.security;

import com.eCommerce.gateway.filter.JwtAuthWebFilter;
import com.eCommerce.gateway.manager.GatewayAuthorizationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewaySecurityConfig {

    private final GatewayAuthorizationManager authorizationManager;
    private final JwtAuthWebFilter jwtAuthWebFilter;

    @Bean
    @Primary
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // ❶ Đăng ký JWT WebFilter đúng layer AUTHENTICATION
                .addFilterBefore(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                // ❷ Khai báo path public
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(SecurityWhiteList.PUBLIC_PATHS).permitAll()
                        .anyExchange().access(authorizationManager)
                )
                .build();
    }
}
