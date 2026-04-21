package com.eCommerce.order.order.repository;

import com.eCommerce.order.order.model.Order;
import com.eCommerce.order.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Lịch sử đơn hàng của user, sắp xếp mới nhất trước
    Page<Order> findByUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);

    // Tìm theo reference (mã đơn hàng hiển thị cho user)
    Optional<Order> findByReference(String reference);

    // Tìm theo orderId và userId — đảm bảo user chỉ xem order của mình
    Optional<Order> findByIdAndUserId(Integer id, Long userId);

    // Update status — dùng khi consume Kafka event
    // Không dùng @Modifying query để tránh bypass Hibernate dirty checking
    // Load entity → set status → save là đủ với traffic thấp
}
