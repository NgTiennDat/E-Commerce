package com.eCommerce.product.service;

import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.request.CategoryRequest;
import com.eCommerce.product.model.request.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories(Boolean isActive);

    CategoryDto getCategoryDetail(Long categoryId);

    CategoryDto getCategoryBySlug(String slug);

    void createCategory(CategoryRequest request);

    void updateCategory(Long categoryId, CategoryRequest categoryDto);

    void deleteCategory(Long categoryId);

    void updateCategoryStatus(Long categoryId, String status);

}
