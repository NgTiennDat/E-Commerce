package com.eCommerce.gateway.manager;

import com.eCommerce.gateway.client.RbacClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
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

        return authentication
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> rbacClient.hasPermission(auth.getName(), method, path)
                        .map(AuthorizationDecision::new)
                )
                .switchIfEmpty(Mono.just(new AuthorizationDecision(false)));
    }
}
