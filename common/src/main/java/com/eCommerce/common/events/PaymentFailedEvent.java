package com.eCommerce.common.events;

import java.math.BigDecimal;

/**
 * Published bởi Payment Service khi thanh toán thất bại.
 *
 * Consumer:
 *   - Order Service        → update status PENDING → CANCELLED
 *   - Notification Service → gửi email thông báo thất bại
 */
public record PaymentFailedEvent(
        Long orderId,
        String orderReference,
        Long userId,
        String userEmail,
        BigDecimal totalAmount,
        String reason
) {}
