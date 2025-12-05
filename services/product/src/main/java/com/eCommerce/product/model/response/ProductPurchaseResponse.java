package com.eCommerce.product.model.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductPurchaseResponse {

    private Long productId;

    private String sku;

    private String name;

    private String shortDescription;

    private String description;

    private BigDecimal price;        // Giá gốc
    private BigDecimal finalPrice;   // Giá sau giảm
    private Integer discountPercent;

    private Integer quantity;        // Số lượng mua
    private Integer availableQuantity; // Tồn kho còn lại sau khi mua

    private BigDecimal totalPrice;   // finalPrice * quantity

    private String imageUrl;
}
