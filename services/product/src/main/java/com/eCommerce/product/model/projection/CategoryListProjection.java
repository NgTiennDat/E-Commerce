package com.eCommerce.product.model.projection;

public interface CategoryListProjection {
    Long getId();
    String getName();
    String getSlug();
    Boolean getIsActive();
    Long getParentId();
    Boolean getHasChildren();
}
