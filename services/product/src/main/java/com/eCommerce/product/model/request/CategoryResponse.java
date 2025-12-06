package com.eCommerce.product.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private String icon;
    private Boolean isActive;
}