package com.eCommerce.order.order.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.order.order.model.request.CreateOrderRequest;
import com.eCommerce.order.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * userId và userEmail được lấy từ header do Gateway forward.
 * Gateway extract từ JWT sau khi validate — Order Service không cần validate JWT lại.
 *
 * Header convention:
 *   X-User-Id    → userId (Long)
 *   X-User-Email → userEmail (String)
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Email") String userEmail
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Response.ofSucceeded(
                        orderService.createOrder(request, userId, userEmail)
                ));
    }

    @GetMapping
    public ResponseEntity<?> getMyOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                Response.ofSucceeded(orderService.getOrdersByUser(userId, page, size))
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(
            @PathVariable Integer orderId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(
                Response.ofSucceeded(orderService.getOrderDetail(orderId, userId))
        );
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Integer orderId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(Response.ofSucceeded());
    }
}
