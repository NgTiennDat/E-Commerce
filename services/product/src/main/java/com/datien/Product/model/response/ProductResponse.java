package com.datien.Product.model.response;

import com.datien.Product.model.Category;
import com.datien.Product.model.dto.CategoryDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private double availableQuantity;
    private BigDecimal price;
    private CategoryDto categoryDto;
}
