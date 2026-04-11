package com.eCommerce.product.model.request;

import com.eCommerce.product.model.enums.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductSearchRequest extends PageInfoRequest {
    private String keyword;
    private String categoryName;
    private ProductStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private Boolean isFeatured;
    private Boolean isNew;
}
