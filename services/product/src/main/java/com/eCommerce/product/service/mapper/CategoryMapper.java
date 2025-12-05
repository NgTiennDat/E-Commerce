package com.eCommerce.product.service.mapper;

import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.entity.Category;

import java.util.List;
import java.util.stream.Collectors;

public final class CategoryMapper {

    private CategoryMapper() {
        // Prevent instantiation
    }

    /**
     * Convert Category entity → CategoryDto
     */
    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(category.getSlug())
                .imageUrl(category.getImageUrl())
                .icon(category.getIcon())
                .isActive(category.getIsActive())
                .build();
    }

    /**
     * Convert CategoryDto → Category entity
     * (Only use if API cho phép tạo/update category)
     */
    public static Category toEntity(CategoryDto dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .id(dto.getId()) // Optional: only set if used for update
                .name(dto.getName())
                .description(dto.getDescription())
                .slug(dto.getSlug())
                .imageUrl(dto.getImageUrl())
                .icon(dto.getIcon())
                .isActive(dto.getIsActive())
                .build();
    }

    /**
     * Convert list Category → list CategoryDto
     */
    public static List<CategoryDto> toDtoList(List<Category> categories) {
        if (categories == null) {
            return List.of();
        }
        return categories.stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }
}
