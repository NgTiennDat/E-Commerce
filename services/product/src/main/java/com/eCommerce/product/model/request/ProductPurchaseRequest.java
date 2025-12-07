package com.eCommerce.product.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductPurchaseRequest {

    @NotNull(message = "Product ID is mandatory")
    private Long productId;

    @NotNull(message = "Quantity is mandatory")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
