package com.eCommerce.order.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

/**
 * Feign client gọi Product Service để:
 *   1. Validate product tồn tại và đang ACTIVE
 *   2. Lấy giá hiện tại (tránh client gửi giá tự chế)
 *   3. Kiểm tra còn đủ stock không
 *
 * Giá được lấy từ BE tại thời điểm đặt hàng — không tin giá từ client.
 */
@FeignClient(name = "product-service", path = "/api/v1/products")
public interface ProductClient {

    @GetMapping("/{productId}")
    ProductResponse getProduct(@PathVariable("productId") Long productId);

    @Getter
    @Setter
    class ProductResponse {
        private Long id;
        private String name;
        private String sku;
        private BigDecimal price;
        private BigDecimal finalPrice;  // giá sau discount
        private Integer availableQuantity;
        private String status;
        private Boolean isDeleted;
    }
}
