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
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication,
                                             AuthorizationContext context) {

        ServerWebExchange exchange = context.getExchange();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        // Các path public đã được permitAll ở SecurityConfig
        // nên AuthorizationManager sẽ KHÔNG được gọi cho các path đó.

        return authentication
                .doOnNext(auth -> System.out.println(">> Auth in manager: " + auth))
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    System.out.println(">> CALL RBAC with " + auth.getName() + " " + method + " " + path);
                    return rbacClient.hasPermission(auth.getName(), method, path)
                            .map(AuthorizationDecision::new);
                })
                .defaultIfEmpty(new AuthorizationDecision(false)
        );
    }
}
