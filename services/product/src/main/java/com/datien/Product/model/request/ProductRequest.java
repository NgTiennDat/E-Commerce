package com.datien.Product.model.request;

import com.datien.Product.model.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private double availableQuantity;
    private BigDecimal price;
    private Category category;
}
