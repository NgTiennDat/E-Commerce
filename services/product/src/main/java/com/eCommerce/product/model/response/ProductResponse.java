package com.eCommerce.product.model.response;

import com.eCommerce.product.model.dto.CategoryDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {

    private Long id;

    private String sku;

    private String name;

    private String shortDescription;

    private String description;

    private BigDecimal price;        // Giá gốc
    private BigDecimal finalPrice;   // Giá sau giảm
    private Integer discountPercent;

    private Integer availableQuantity;
    private Boolean inStock;

    private String imageUrl;
    private String brand;

    private Double rating;
    private Integer ratingCount;

    private Boolean isFeatured;
    private Boolean isNew;

    private CategoryDto category;
}
