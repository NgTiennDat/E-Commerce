package com.eCommerce.gateway.security;

import com.eCommerce.gateway.filter.JwtAuthWebFilter;
import com.eCommerce.gateway.manager.GatewayAuthorizationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .addFilterBefore(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(SecurityWhiteList.PUBLIC_PATHS).permitAll()
                        .anyExchange().access(authorizationManager)
                )
                .build();
    }
}
