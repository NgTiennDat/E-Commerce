package com.eCommerce.product.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategorySearchRequest extends PageInfoRequest{

    private String keyword;
    private Boolean isActive;

    private Long parentId;
    private Boolean hasChildren;
}
