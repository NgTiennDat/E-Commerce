package com.eCommerce.gateway.security;

import com.eCommerce.gateway.manager.GatewayAuthorizationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewaySecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/actuator/**",
            "/eureka/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    private final GatewayAuthorizationManager authorizationManager;

    @Bean
    @Primary
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        .anyExchange()
                        .access(authorizationManager)
                )
                .build();
    }
}
