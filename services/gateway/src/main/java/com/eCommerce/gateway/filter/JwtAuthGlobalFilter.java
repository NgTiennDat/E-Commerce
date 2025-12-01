package com.eCommerce.gateway.filter;

import com.eCommerce.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Các path public không cần JWT (login, register, swagger, v.v.)
    private static final List<String> WHITE_LIST = List.of(
            "/auth-service/api/v1/auth/login",
            "/auth-service/api/v1/users/register",
            "/auth-service/api/v1/auth/refresh-token",
            "/api/v1/products/**",
            "/eureka",
            "/actuator",
            "/swagger", "/v3/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("[Gateway] Incoming request path: {}", path);

        // Nếu path thuộc whitelist thì bỏ qua filter
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        if (!jwtUtils.isTokenValid(token)) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        String username = jwtUtils.extractUsername(token);
        var roles = jwtUtils.extractRoles(token);

        // Đính thêm header để các service phía sau đọc được
        var mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Name", username)
                .header("X-User-Roles", String.join(",", roles))
                .build();

        var mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange);
    }

    private boolean isWhitelisted(String path) {
        boolean result = WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        log.info("[Gateway] Path = {}, isWhitelisted = {}", path, result);
        return result;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                  "code": 401,
                  "message": "%s"
                }
                """.formatted(message);

        var buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
