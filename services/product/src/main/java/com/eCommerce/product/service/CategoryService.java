package com.eCommerce.product.service;

import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.request.CategoryRequest;
import com.eCommerce.product.model.request.CategorySearchRequest;
import org.springframework.data.domain.Page;

public interface CategoryService {

    Page<CategoryDto> getCategories(CategorySearchRequest request);

    CategoryDto getCategoryDetail(Long categoryId);

    CategoryDto getCategoryBySlug(String slug);

    Long createCategory(CategoryRequest request);

    void updateCategory(Long categoryId, CategoryRequest request);

    void deleteCategory(Long categoryId);

    void updateCategoryStatus(Long categoryId, Boolean isActive);
}
