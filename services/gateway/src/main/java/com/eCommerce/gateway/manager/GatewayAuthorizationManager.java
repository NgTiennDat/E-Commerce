package com.eCommerce.gateway.manager;

import com.eCommerce.gateway.client.RbacClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GatewayAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final RbacClient rbacClient;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {

        ServerWebExchange exchange = context.getExchange();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return Mono.just(new AuthorizationDecision(true));
        }

        return authentication
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> rbacClient.hasPermission(auth.getName(), method, path)
                        .map(AuthorizationDecision::new))
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth")
                || path.startsWith("/actuator")
                || path.startsWith("/eureka")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs");
    }
}
