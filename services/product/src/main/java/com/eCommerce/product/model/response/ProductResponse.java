package com.eCommerce.product.model.response;

import com.eCommerce.product.model.dto.CategoryDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private Integer availableQuantity;
    private BigDecimal price;
    private CategoryDto categoryDto;
}
