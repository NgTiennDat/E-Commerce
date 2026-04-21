package com.eCommerce.order.order.model;

import com.eCommerce.order.order.model.enums.OrderStatus;
import com.eCommerce.order.orderline.model.OrderLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tại sao không dùng @Data trên JPA entity?
 *
 * @Data sinh ra @EqualsAndHashCode dựa trên TẤT CẢ fields.
 * Với JPA entity, điều này gây ra 3 vấn đề:
 *
 * 1. StackOverflowError với bidirectional relationship:
 *    Order.equals() → so sánh orderLines → OrderLine.equals() → so sánh order
 *    → Order.equals() lại được gọi → vòng lặp vô tận.
 *
 * 2. Hibernate Proxy bị broken:
 *    Proxy là subclass của entity. getClass() khác nhau → equals() luôn false
 *    dù cùng ID và data.
 *
 * 3. HashSet/HashMap bị broken sau persist:
 *    Entity thêm vào Set khi id=null, sau persist id có giá trị
 *    → hashCode thay đổi → entity "biến mất" khỏi Set.
 *
 * @ToString sinh ra LazyInitializationException:
 *    toString() access lazy collection ngoài transaction → exception.
 *
 * Giải pháp: dùng @Getter @Setter riêng, implement equals/hashCode
 * chỉ dựa trên id với null-safe check.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Mã đơn hàng hiển thị cho user, dạng: ORD-20240101-XXXXX
    @Column(unique = true, nullable = false)
    private String reference;

    // userId từ auth-service (JWT subject)
    @Column(nullable = false)
    private Long userId;

    // Email để gửi notification — lưu lại tránh phải lookup lại
    @Column(nullable = false)
    private String userEmail;

    // customerId từ MongoDB Customer Service
    @Column(nullable = false)
    private String customerId;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order")
    private List<OrderLine> orderLines;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    // -----------------------------------------------------------------------
    // equals/hashCode — chỉ dựa trên id
    // -----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // instanceof thay vì getClass() để Hibernate proxy hoạt động đúng
        if (!(o instanceof Order other)) return false;
        // id null = entity chưa persist → không bằng bất kỳ entity nào khác
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // Constant per class — đảm bảo hashCode không thay đổi sau persist
        // Trade-off: HashSet performance kém hơn, nhưng correctness được đảm bảo
        return getClass().hashCode();
    }
}
