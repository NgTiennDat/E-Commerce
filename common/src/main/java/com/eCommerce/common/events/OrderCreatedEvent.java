package com.eCommerce.common.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Published bởi Order Service sau khi tạo order thành công.
 *
 * Consumer:
 *   - Payment Service  → dùng orderId, totalAmount, paymentMethod để xử lý thanh toán
 *   - Notification Service → dùng userEmail, orderReference để gửi email xác nhận
 *
 * Tại sao embed đủ thông tin thay vì chỉ gửi orderId?
 * Tránh consumer phải gọi lại Order Service để lấy data → giảm coupling,
 * tăng resilience (consumer hoạt động độc lập dù Order Service tạm down).
 */
public record OrderCreatedEvent(
        Long orderId,
        String orderReference,
        Long userId,
        String userEmail,
        String customerId,
        BigDecimal totalAmount,
        String paymentMethod,
        List<OrderItem> items
) {
    /**
     * Thông tin từng sản phẩm trong đơn hàng.
     * Inventory Service dùng productId + quantity để trừ tồn kho.
     */
    public record OrderItem(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}
