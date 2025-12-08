package com.eCommerce.product.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "SKU is mandatory")
    private String sku;

    @NotBlank(message = "Product name is mandatory")
    private String name;

    @NotBlank(message = "Short description is mandatory")
    private String shortDescription;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotNull(message = "Price is mandatory")
    private BigDecimal price;

    private Integer discountPercent;

    @NotNull(message = "Available quantity is mandatory")
    @Min(value = 0, message = "Available quantity cannot be negative")
    private Integer availableQuantity;

    private String imageUrl;

    @NotBlank(message = "Brand is mandatory")
    private String brand;

    private Boolean isFeatured;

    private Boolean isNew;

    @NotNull(message = "Category is mandatory")
    private Long categoryId;

}
