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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Các path public không cần JWT
    private static final List<String> WHITE_LIST = List.of(
            // Auth public
            "/api/v1/auth/login",
            "/api/v1/users/register",
            "/api/v1/auth/refresh-token",

            // Sản phẩm public cho khách (tùy chiến lược, ví dụ cho phép GET)
            "/api/v1/products",
            "/api/v1/products/*/related",

            // system
            "/eureka/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("[Gateway] Incoming request path: {}", path);

        // Nếu path thuộc whitelist thì bỏ qua filter (không cần JWT)
        if (isWhitelisted(path)) {
            log.debug("[Gateway] Path {} is whitelisted, skip JWT filter", path);
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
        List<String> roles = jwtUtils.extractRoles(token);
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        // Đính thêm header để các service phía sau đọc được (nếu muốn)
        var mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Name", username)
                .header("X-User-Roles", String.join(",", roles))
                .build();

        var mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // ⚡ Quan trọng: set Authentication vào ReactiveSecurityContextHolder
        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
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
