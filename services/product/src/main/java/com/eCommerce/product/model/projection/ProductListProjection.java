package com.eCommerce.product.model.projection;

import java.math.BigDecimal;

public interface ProductListProjection {

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

    Long getCategoryId();
    String getCategoryName();
    String getCategoryDescription();
    String getCategorySlug();
    String getCategoryImageUrl();
    String getCategoryIcon();
    Boolean getCategoryIsActive();

}
