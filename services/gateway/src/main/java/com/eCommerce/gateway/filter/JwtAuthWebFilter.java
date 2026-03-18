package com.eCommerce.gateway.filter;

import com.eCommerce.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthWebFilter implements WebFilter {

    private static final String AUTHORIZATION_PREFIX = "Bearer ";
    private final JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(AUTHORIZATION_PREFIX)) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(AUTHORIZATION_PREFIX.length()).trim();
        if (token.isEmpty() || !jwtUtils.isTokenValid(token)) {
            return chain.filter(exchange);
        }

        String username = jwtUtils.extractUsername(token);
        List<String> roles = jwtUtils.extractRoles(token);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                roles.stream().map(SimpleGrantedAuthority::new).toList()
        );

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}
