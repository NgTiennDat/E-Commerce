package com.eCommerce.common.events;

import java.math.BigDecimal;

public record PaymentSucceededEvent(
        Long orderId,
        Long userId,
        BigDecimal totalAmount
) {
}
