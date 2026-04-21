package com.eCommerce.order.order.service.impl;

import com.eCommerce.common.events.OrderCreatedEvent;
import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.order.client.ProductClient;
import com.eCommerce.order.customer.CustomerCacheService;
import com.eCommerce.order.order.model.Order;
import com.eCommerce.order.order.model.PaymentMethod;
import com.eCommerce.order.order.model.enums.OrderStatus;
import com.eCommerce.order.order.model.request.CreateOrderRequest;
import com.eCommerce.order.order.model.response.OrderResponse;
import com.eCommerce.order.order.repository.OrderRepository;
import com.eCommerce.order.order.service.OrderService;
import com.eCommerce.order.orderline.model.OrderLine;
import com.eCommerce.order.orderline.repository.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_TOPIC = "order-created";
    private static final DateTimeFormatter REF_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final ProductClient productClient;
    private final CustomerCacheService customerCacheService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Tạo đơn hàng:
     *   1. Lookup customerId từ cache/Customer Service
     *   2. Validate từng sản phẩm qua Product Service (tồn tại, còn hàng)
     *   3. Tính tổng tiền từ giá BE (không tin giá từ client)
     *   4. Lưu Order + OrderLines vào DB
     *   5. Publish OrderCreatedEvent → Kafka
     */
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request,
                                     Long userId, String userEmail) {
        // 1. Lookup customerId
        String customerId = customerCacheService.getCustomerId(userEmail);

        // 2. Validate products và tính tổng tiền
        List<ValidatedItem> validatedItems = validateAndPriceItems(request.getItems());
        BigDecimal totalAmount = validatedItems.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Tạo Order
        Order order = Order.builder()
                .reference(generateReference())
                .userId(userId)
                .userEmail(userEmail)
                .customerId(customerId)
                .totalAmount(totalAmount)
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .status(OrderStatus.PENDING)
                .build();
        Order savedOrder = orderRepository.save(order);

        // 4. Tạo OrderLines
        List<OrderLine> orderLines = validatedItems.stream()
                .map(item -> OrderLine.builder()
                        .productId(item.productId().intValue())
                        .quantity(item.quantity())
                        .order(savedOrder)
                        .build())
                .toList();
        orderLineRepository.saveAll(orderLines);

        // 5. Publish event
        OrderCreatedEvent event = buildOrderCreatedEvent(savedOrder, validatedItems, userEmail);
        kafkaTemplate.send(ORDER_TOPIC, String.valueOf(savedOrder.getId()), event);
        log.info("Order created id={} reference={}", savedOrder.getId(), savedOrder.getReference());

        return toResponse(savedOrder, validatedItems);
    }

    @Override
    public Page<OrderResponse> getOrdersByUser(Long userId, int page, int size) {
        return orderRepository
                .findByUserIdOrderByCreatedDateDesc(userId, PageRequest.of(page, size))
                .map(order -> toResponse(order, List.of()));
    }

    @Override
    public OrderResponse getOrderDetail(Integer orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new CustomException(ResponseCode.ORDER_NOT_FOUND));
        return toResponse(order, List.of());
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new CustomException(ResponseCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CustomException(ResponseCode.ORDER_CANNOT_BE_CANCELLED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order cancelled by user id={}", orderId);
    }

    @Override
    @Transactional
    public void confirmOrder(Integer orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order confirmed id={}", orderId);
        });
    }

    @Override
    @Transactional
    public void cancelOrderByPaymentFailure(Integer orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order cancelled due to payment failure id={}", orderId);
        });
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Validate từng sản phẩm qua Product Service:
     *   - Sản phẩm tồn tại và ACTIVE
     *   - Còn đủ số lượng
     *   - Lấy giá finalPrice (sau discount) từ BE
     */
    private List<ValidatedItem> validateAndPriceItems(
            List<CreateOrderRequest.OrderItemRequest> items) {

        return items.stream().map(item -> {
            ProductClient.ProductResponse product =
                    productClient.getProduct(item.getProductId());

            if (product == null || Boolean.TRUE.equals(product.getIsDeleted())) {
                throw new CustomException(ResponseCode.PRODUCT_NOT_FOUND);
            }
            if (!"ACTIVE".equals(product.getStatus())) {
                throw new CustomException(ResponseCode.PRODUCT_NOT_FOUND);
            }
            if (product.getAvailableQuantity() < item.getQuantity()) {
                throw new CustomException(ResponseCode.PRODUCT_QUANTITY_NOT_ENOUGH);
            }

            BigDecimal unitPrice = product.getFinalPrice() != null
                    ? product.getFinalPrice()
                    : product.getPrice();

            return new ValidatedItem(
                    product.getId(),
                    product.getName(),
                    item.getQuantity(),
                    unitPrice
            );
        }).toList();
    }

    /**
     * Tạo mã đơn hàng dạng: ORD-20240101-A1B2C3
     * Đủ unique cho demo, production nên dùng sequence DB.
     */
    private String generateReference() {
        String date = LocalDateTime.now().format(REF_FORMATTER);
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + date + "-" + suffix;
    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order,
                                                      List<ValidatedItem> items,
                                                      String userEmail) {
        List<OrderCreatedEvent.OrderItem> eventItems = items.stream()
                .map(i -> new OrderCreatedEvent.OrderItem(
                        i.productId(), i.productName(), i.quantity(), i.unitPrice()
                ))
                .toList();

        return new OrderCreatedEvent(
                order.getId().longValue(),
                order.getReference(),
                order.getUserId(),
                userEmail,
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getPaymentMethod().name(),
                eventItems
        );
    }

    private OrderResponse toResponse(Order order, List<ValidatedItem> items) {
        List<OrderResponse.OrderLineResponse> lineResponses = items.stream()
                .map(item -> OrderResponse.OrderLineResponse.builder()
                        .productId(item.productId())
                        .productName(item.productName())
                        .quantity(item.quantity())
                        .unitPrice(item.unitPrice())
                        .subtotal(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod().name())
                .items(lineResponses)
                .createdDate(order.getCreatedDate())
                .build();
    }

    // Record nội bộ — chứa thông tin sản phẩm đã validate
    private record ValidatedItem(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}
