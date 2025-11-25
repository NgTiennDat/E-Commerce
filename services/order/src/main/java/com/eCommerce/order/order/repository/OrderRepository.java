package com.eCommerce.order.order.repository;

import com.eCommerce.order.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
