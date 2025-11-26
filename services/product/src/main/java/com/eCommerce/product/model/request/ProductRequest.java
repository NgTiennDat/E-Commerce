package com.eCommerce.product.model.request;

import com.eCommerce.product.model.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private Integer availableQuantity;
    private BigDecimal price;
    private Category category;
}
