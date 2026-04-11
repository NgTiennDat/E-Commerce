package com.eCommerce.order.orderline.service;

import com.eCommerce.order.order.model.Order;
import com.eCommerce.order.orderline.model.OrderLine;
import com.eCommerce.order.orderline.model.OrderLineRequest;
import com.eCommerce.order.orderline.model.OrderLineResponse;
import com.eCommerce.order.orderline.repository.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderLineService {
    private final OrderLineRepository orderLineRepository;

    public Integer saveOrderLine(OrderLineRequest request) {
        // Tên biến đổi từ "order" → "orderLine" — tránh nhầm lẫn với Order entity.
        // Không set id thủ công — id do DB auto-generate qua GenerationType.IDENTITY.
        // Bug cũ: order.setId(request.getOrderId()) đang set id của OrderLine = orderId — sai hoàn toàn.
        var orderLine = new OrderLine();
        orderLine.setProductId(request.getProductId());
        orderLine.setQuantity(request.getQuantity());
        orderLine.setOrder(
                Order.builder()
                        .id(request.getOrderId())
                        .build()
        );
        return orderLineRepository.save(orderLine).getId();
    }

    public List<OrderLineResponse> findAllByOrderId(Integer orderId) {
        return orderLineRepository.findAllByOrderId(orderId)
                .stream()
                .map(this::toOrderLineResponse)
                .toList();
    }

    public OrderLineResponse toOrderLineResponse(OrderLine orderLine) {
        return new OrderLineResponse(
                orderLine.getId(),
                orderLine.getQuantity()
        );
    }
}
