package com.datien.Product.model.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductPurchaseResponse{
    private Integer productId;
    private String name;
    private String description;
    private BigDecimal price;
    private double quantity;
}
