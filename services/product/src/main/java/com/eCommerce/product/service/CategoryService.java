package com.eCommerce.product.service;

import com.eCommerce.product.model.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories();

    Object getCategoryDetail(Long categoryId);
}
