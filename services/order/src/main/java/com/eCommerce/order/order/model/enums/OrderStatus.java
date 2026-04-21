package com.eCommerce.order.order.model.enums;

/**
 * Vòng đời của đơn hàng:
 *
 *   PENDING → CONFIRMED → SHIPPED → DELIVERED
 *          ↘ CANCELLED
 *
 * PENDING:   Đơn vừa tạo, chờ thanh toán
 * CONFIRMED: Thanh toán thành công, đang chuẩn bị hàng
 * SHIPPED:   Đã giao cho đơn vị vận chuyển
 * DELIVERED: Khách đã nhận hàng
 * CANCELLED: Đơn bị hủy (payment thất bại hoặc user hủy)
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
