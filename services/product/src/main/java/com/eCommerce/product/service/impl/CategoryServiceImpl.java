package com.eCommerce.product.service.impl;

import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.request.CategoryRequest;
import com.eCommerce.product.repository.CategoryRepository;
import com.eCommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getCategories(Boolean isActive) {
        try {
            return categoryRepository.findAllCategory(isActive);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CategoryDto getCategoryDetail(Long categoryId) {
        try {
            return categoryRepository.findCateById(categoryId);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CategoryDto getCategoryBySlug(String slug) {
        try {
            return categoryRepository.findCateBySlug(slug);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void createCategory(CategoryRequest request) {
        try {

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void updateCategory(Long categoryId, CategoryRequest categoryDto) {
        try {

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteCategory(Long categoryId) {
        try {
            var category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CATEGORY_NOT_FOUND));
            category.setIsDeleted(true);
            categoryRepository.save(category);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void updateCategoryStatus(Long categoryId, String status) {
        try {
            var category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CATEGORY_NOT_FOUND));
            category.setIsActive(true);
            categoryRepository.save(category);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

}
