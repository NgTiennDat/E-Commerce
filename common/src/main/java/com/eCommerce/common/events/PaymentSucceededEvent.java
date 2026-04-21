package com.eCommerce.common.events;

import java.math.BigDecimal;

/**
 * Published bởi Payment Service sau khi thanh toán thành công.
 *
 * Consumer:
 *   - Order Service        → update status PENDING → CONFIRMED
 *   - Inventory Service    → trừ tồn kho
 *   - Notification Service → gửi email xác nhận thanh toán
 */
public record PaymentSucceededEvent(
        Long orderId,
        String orderReference,
        Long userId,
        String userEmail,
        BigDecimal totalAmount,
        String transactionId
) {}
