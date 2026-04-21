package com.eCommerce.order.order.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request tạo đơn hàng.
 *
 * userId và userEmail KHÔNG nhận từ client — lấy từ JWT header
 * do Gateway forward (X-User-Id, X-User-Email).
 * Tránh client tự khai báo userId của người khác.
 */
@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @Getter
    @Setter
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}
