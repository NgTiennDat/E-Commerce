package com.eCommerce.product.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "category")
public class Category extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    // SEO friendly URL: vd: "keyboards", "monitors"
    @Column(unique = true, name = "slug")
    private String slug;

    // Ảnh đại diện cho category (hiển thị ở UI)
    @Column(name = "image_url")
    private String imageUrl;

    // Icon nhỏ cho menu/sidebar (có thể là tên icon hoặc URL)\
    @Column(name = "icon")
    private String icon;

    // Có đang active để hiển thị ở storefront không
    @Column(name = "is_active")
    private Boolean isActive;

    // Thứ tự hiển thị (sort trên UI)
    @Column(name = "display_order")
    private Integer displayOrder;

    // Danh mục cha (để tạo tree nếu sau này cần: Keyboard ⟶ Mechanical, Wireless)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;
}
