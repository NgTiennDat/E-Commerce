package com.eCommerce.order.order.model.response;

import com.eCommerce.order.order.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Integer id;
    private String reference;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private List<OrderLineResponse> items;
    private LocalDateTime createdDate;

    @Getter
    @Builder
    public static class OrderLineResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
