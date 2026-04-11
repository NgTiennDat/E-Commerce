package com.eCommerce.product.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base class cho JPA auditing — tự động set createdAt, updatedAt,
 * createdBy, updatedBy khi entity được persist/update.
 *
 * Yêu cầu:
 *   1. @EnableJpaAuditing trên @SpringBootApplication
 *   2. Bean AuditorAware<String> để cung cấp username hiện tại
 *
 * Tại sao xóa @PrePersist / @PreUpdate?
 *   Trước đây dùng @PrePersist để set isDeleted = false khi null.
 *   Field initializer (= false) làm điều tương tự đơn giản hơn —
 *   default được set ngay khi object được tạo, không cần lifecycle callback.
 *   @PrePersist chỉ nên dùng khi cần logic phức tạp hơn set default value.
 */
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Audit {

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Field initializer thay thế @PrePersist — đơn giản và rõ ràng hơn
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
