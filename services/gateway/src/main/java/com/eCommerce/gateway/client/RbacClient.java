package com.eCommerce.gateway.client;

import com.eCommerce.gateway.request.RbacCheckRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RbacClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth-service.base-url}")
    private String authServiceBaseUrl; // vd: http://AUTH-SERVICE (qua Eureka) hoặc http://localhost:8081

    public Mono<Boolean> hasPermission(String username, String method, String path) {
        RbacCheckRequest body = new RbacCheckRequest(username, method, path);

        return webClientBuilder.build()
                .post()
                .uri(authServiceBaseUrl + "/internal/rbac/check")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(ex -> {
                    // Nếu auth-service lỗi thì fail-safe: từ chối
                    return Mono.just(false);
                });
    }
}
