package com.datien.Product.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductPurchaseRequest {

    @NotNull(message = "Product is mandatory")
    private Integer productId;

    @NotNull(message = "Quantity is mandatory")
    private double quantity;
}
