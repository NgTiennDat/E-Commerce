package com.eCommerce.product.seeder.client;

import com.eCommerce.product.seeder.dto.DummyJsonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Chỉ được tạo khi seeder.enabled=true.
 * Tránh lỗi "Could not resolve placeholder 'seeder.dummyjson.base-url'"
 * khi seeder bị tắt và property không được cung cấp.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "seeder.enabled", havingValue = "true")
public class DummyJsonClient {

    private static final String PRODUCTS_PATH = "/products?limit={limit}&skip=0&select=title,description,category,price,discountPercentage,rating,stock,brand,thumbnail,images";

    private final RestClient restClient;

    public DummyJsonClient(@Value("${seeder.dummyjson.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public DummyJsonResponse fetchProducts(int limit) {
        log.info("Fetching {} products from DummyJSON...", limit);
        return restClient.get()
                .uri(PRODUCTS_PATH, limit)
                .retrieve()
                .body(DummyJsonResponse.class);
    }
}
