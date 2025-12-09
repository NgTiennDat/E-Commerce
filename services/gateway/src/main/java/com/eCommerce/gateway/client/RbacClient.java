package com.eCommerce.gateway.client;

import com.eCommerce.gateway.request.RbacCheckRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbacClient {

    private static final String RBAC_CHECK_PATH = "/internal/rbac/check";

    private final WebClient.Builder webClientBuilder;

    @Value("${auth-service.base-url}")
    private String authServiceBaseUrl;

    /**
     * Gọi sang Auth-Service để kiểm tra RBAC.
     */
    public Mono<Boolean> hasPermission(String username, String method, String path) {
        RbacCheckRequest body = new RbacCheckRequest(username, method, path);

        return webClientBuilder.build()
                .post()
                .uri(authServiceBaseUrl + RBAC_CHECK_PATH)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(ex -> {
                    log.error("Failed to check permission for user '{}' on {} {}",
                            username, method, path, ex);
                    return Mono.just(Boolean.FALSE);
                });
    }
}
