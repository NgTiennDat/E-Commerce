package com.eCommerce.product.service.impl;

import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.entity.Category;
import com.eCommerce.product.model.projection.CategoryListProjection;
import com.eCommerce.product.model.request.CategoryRequest;
import com.eCommerce.product.model.request.CategorySearchRequest;
import com.eCommerce.product.repository.CategoryRepository;
import com.eCommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Category orchestration.
 * Auditing được handle tự động bởi Spring Data JPA Auditing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<CategoryDto> getCategories(CategorySearchRequest request) {
        CategorySearchRequest safeRequest = request == null ? new CategorySearchRequest() : request;
        Pageable pageable = safeRequest.toPageable();

        Page<CategoryListProjection> page = categoryRepository.searchCategories(
                safeRequest.getKeyword(),
                safeRequest.getIsActive(),
                safeRequest.getParentId(),
                safeRequest.getHasChildren(),
                pageable
        );

        return page.map(this::mapListProjection);
    }

    @Override
    public CategoryDto getCategoryDetail(Long categoryId) {
        CategoryDto dto = categoryRepository.findCateById(categoryId);
        if (dto == null) {
            throw new CustomException(ResponseCode.CATEGORY_NOT_FOUND);
        }
        return dto;
    }

    @Override
    public CategoryDto getCategoryBySlug(String slug) {
        CategoryDto dto = categoryRepository.findCateBySlug(slug);
        if (dto == null) {
            throw new CustomException(ResponseCode.CATEGORY_NOT_FOUND);
        }
        return dto;
    }

    /**
     * Creates a category ensuring slug uniqueness.
     * Parent validation can be added when hierarchical categories go live.
     */
    @Override
    public Long createCategory(CategoryRequest request) {
        if (categoryRepository.existsBySlugAndIsDeletedFalse(request.getSlug())) {
            throw new CustomException(ResponseCode.INVALID_REQUEST);
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(request.getSlug());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);

        Long id = categoryRepository.save(category).getId();
        log.info("Category created slug={}, id={}", category.getSlug(), id);
        return id;
    }

    @Override
    public void updateCategory(Long categoryId, CategoryRequest request) {
        Category category = getCategoryOrThrow(categoryId);

        boolean slugChanged = !category.getSlug().equals(request.getSlug());
        if (slugChanged && categoryRepository.existsBySlugAndIsDeletedFalse(request.getSlug())) {
            throw new CustomException(ResponseCode.INVALID_REQUEST);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(request.getSlug());
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        categoryRepository.save(category);
        log.info("Category updated id={}", categoryId);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryOrThrow(categoryId);
        category.setIsDeleted(true);
        categoryRepository.save(category);
        log.info("Category soft-deleted id={}", categoryId);
    }

    @Override
    public void updateCategoryStatus(Long categoryId, Boolean isActive) {
        Category category = getCategoryOrThrow(categoryId);
        category.setIsActive(Boolean.TRUE.equals(isActive));
        categoryRepository.save(category);
        log.info("Category status updated id={} active={}", categoryId, isActive);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findByIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new CustomException(ResponseCode.CATEGORY_NOT_FOUND));
    }

    private CategoryDto mapListProjection(CategoryListProjection projection) {
        CategoryDto dto = new CategoryDto();
        dto.setId(projection.getId());
        dto.setName(projection.getName());
        dto.setSlug(projection.getSlug());
        dto.setIsActive(projection.getIsActive());
        return dto;
    }
}
