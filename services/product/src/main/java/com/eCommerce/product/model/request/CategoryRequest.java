package com.eCommerce.product.model.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryRequest {
    
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private String icon;
    private Boolean isActive;

}
