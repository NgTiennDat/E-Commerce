package com.eCommerce.product.model.entity;

import com.eCommerce.product.model.enumn.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã sản phẩm, dùng cho quản lý, tìm kiếm nội bộ
    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "name")
    private String name;

    // Mô tả ngắn, dùng cho card trong list
    @Column(name = "short_description")
    private String shortDescription;

    // Mô tả chi tiết
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    @Column(name = "price")
    private BigDecimal price;

    // % giảm giá, ví dụ 15 = giảm 15%
    @Column(name = "discount_percent")
    private Integer discountPercent;

    // URL ảnh chính
    @Column(name = "image_url")
    private String imageUrl;

    // Thương hiệu
    @Column(name = "brand")
    private String brand;

    // Điểm rating trung bình (1.0 - 5.0)
    @Column(name = "rating")
    private Double rating;

    // Số lượng đánh giá
    @Column(name = "rating_count")
    private Integer ratingCount;

    // Sản phẩm nổi bật
    @Column(name = "is_featured")
    private Boolean isFeatured;

    // Sản phẩm mới
    @Column(name = "is_new")
    private Boolean isNew;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatus status;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
