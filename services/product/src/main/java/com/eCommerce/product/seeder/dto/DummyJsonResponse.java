package com.eCommerce.product.seeder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonResponse(List<DummyJsonProduct> products) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DummyJsonProduct(
            Long id,
            String title,
            String description,
            String category,
            Double price,
            Double discountPercentage,
            Double rating,
            Integer stock,
            String brand,
            String thumbnail,
            List<String> images
    ) {}
}
