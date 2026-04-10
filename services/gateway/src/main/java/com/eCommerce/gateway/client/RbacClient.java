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

    // Header name được define ở đây vì gateway không phụ thuộc common module của auth-service.
    // Nếu sau này có nhiều internal endpoint, có thể tạo GatewayConstants riêng.
    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final WebClient.Builder webClientBuilder;

    @Value("${auth-service.base-url}")
    private String authServiceBaseUrl;

    // Đọc secret từ config — phải khớp với giá trị bên auth-service
    @Value("${internal.secret}")
    private String internalSecret;

    /**
     * Gọi sang Auth-Service để kiểm tra RBAC.
     * Gửi kèm X-Internal-Secret header để Auth Service xác thực đây là request nội bộ.
     */
    public Mono<Boolean> hasPermission(String username, String method, String path) {
        RbacCheckRequest body = new RbacCheckRequest(username, method, path);

        return webClientBuilder.build()
                .post()
                .uri(authServiceBaseUrl + RBAC_CHECK_PATH)
                .header(INTERNAL_SECRET_HEADER, internalSecret)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnNext(result ->
                        log.info(">> RBAC RESULT at gateway for user={} {} {} => {}",
                                username, method, path, result)
                )
                .onErrorResume(ex -> {
                    log.error("Failed to check permission for user '{}' on {} {}",
                            username, method, path, ex);
                    return Mono.just(Boolean.FALSE);
                });
    }
}
