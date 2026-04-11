package com.eCommerce.order.orderline.model;

import com.eCommerce.order.order.model.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vấn đề cũ:
 *
 * 1. @Data — xem giải thích trong Order.java
 *
 * 2. public fields (id, productId, quantity):
 *    public field trên entity vi phạm encapsulation hoàn toàn.
 *    Bất kỳ code nào cũng có thể set orderLine.id = 999 trực tiếp,
 *    bypass mọi validation và business logic.
 *    JPA cũng có thể hoạt động không đúng với public fields
 *    vì Hibernate expect access qua getter/setter hoặc field access nhất quán.
 *
 * 3. quantity là double:
 *    Số lượng sản phẩm trong đơn hàng không bao giờ là số thập phân
 *    (không thể mua 1.5 cái điện thoại).
 *    double còn có vấn đề precision với phép tính tài chính.
 *    Dùng Integer — rõ ràng về domain, không có precision issue.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id")
    private Integer productId;

    // Integer thay vì double — số lượng sản phẩm luôn là số nguyên
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLine other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
