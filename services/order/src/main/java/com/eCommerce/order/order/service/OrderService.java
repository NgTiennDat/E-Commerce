package com.eCommerce.order.order.service;

import com.eCommerce.order.order.model.request.CreateOrderRequest;
import com.eCommerce.order.order.model.response.OrderResponse;
import org.springframework.data.domain.Page;

public interface OrderService {

    /**
     * Tạo đơn hàng mới.
     *
     * @param request     thông tin sản phẩm và payment method
     * @param userId      lấy từ JWT header (X-User-Id)
     * @param userEmail   lấy từ JWT header (X-User-Email)
     * @return OrderResponse với status PENDING
     */
    OrderResponse createOrder(CreateOrderRequest request, Long userId, String userEmail);

    /**
     * Lấy lịch sử đơn hàng của user.
     */
    Page<OrderResponse> getOrdersByUser(Long userId, int page, int size);

    /**
     * Lấy chi tiết 1 đơn hàng — chỉ owner mới xem được.
     */
    OrderResponse getOrderDetail(Integer orderId, Long userId);

    /**
     * User hủy đơn — chỉ được hủy khi status = PENDING.
     */
    void cancelOrder(Integer orderId, Long userId);

    /**
     * Callback từ Kafka khi payment thành công.
     * Update status PENDING → CONFIRMED.
     */
    void confirmOrder(Integer orderId);

    /**
     * Callback từ Kafka khi payment thất bại.
     * Update status PENDING → CANCELLED.
     */
    void cancelOrderByPaymentFailure(Integer orderId);
}
