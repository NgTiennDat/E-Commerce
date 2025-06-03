package com.example.order.orderline.service;

import com.example.order.order.model.Order;
import com.example.order.orderline.model.OrderLine;
import com.example.order.orderline.model.OrderLineRequest;
import com.example.order.orderline.model.OrderLineResponse;
import com.example.order.orderline.repository.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderLineService {
    private final Logger logger = LogManager.getLogger(OrderLineService.class);
    private final OrderLineRepository orderLineRepository;

    public Integer saveOrderLine(OrderLineRequest request) {
        var order = new OrderLine();
        order.setId(request.getOrderId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setOrder(
                Order.builder()
                        .id(request.getOrderId())
                        .build()
        );
        return orderLineRepository.save(order).getId();
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
