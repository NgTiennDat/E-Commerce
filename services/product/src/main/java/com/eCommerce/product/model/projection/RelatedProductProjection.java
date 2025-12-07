package com.eCommerce.product.model.projection;

import java.math.BigDecimal;

public interface RelatedProductProjection {

    Long getId();
    String getSku();
    String getName();
    String getShortDescription();
    String getDescription();

    BigDecimal getPrice();
    Integer getDiscountPercent();
    Integer getAvailableQuantity();

    String getImageUrl();
    String getBrand();
    Double getRating();
    Integer getRatingCount();
    Boolean getIsFeatured();
    Boolean getIsNew();

    // Category info (đủ cho UI nếu cần)
    Long getCategoryId();
    String getCategoryName();
    String getCategorySlug();
    String getCategoryImageUrl();
    String getCategoryIcon();
}
