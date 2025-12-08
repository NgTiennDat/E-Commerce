package com.eCommerce.product.repository;

import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            
            SELECT new com.eCommerce.product.model.dto.CategoryDto(
                    c.id,
                    c.name,
                    c.description,
                    c.slug,
                    c.imageUrl,
                    c.icon,
                    c.isActive
                )
            FROM Category c
            WHERE (:isActive IS NULL OR :isActive = c.isActive)
            ORDER BY c.createdAt DESC
            """)
    List<CategoryDto> findAllCategory(Boolean isActive);

    @Query("""
            SELECT new com.eCommerce.product.model.dto.CategoryDto(
                    c.id,
                    c.name,
                    c.description,
                    c.slug,
                    c.imageUrl,
                    c.icon,
                    c.isActive
                )
            FROM Category c
            WHERE c.id = :categoryId
            AND c.isActive = true
            """)
    CategoryDto findCateById(Long categoryId);

    @Query("""
            SELECT new com.eCommerce.product.model.dto.CategoryDto(
                    c.id,
                    c.name,
                    c.description,
                    c.slug,
                    c.imageUrl,
                    c.icon,
                    c.isActive
                )
            FROM Category c
            WHERE c.slug = :slug
            AND c.isActive = true
            """)
    CategoryDto findCateBySlug(String slug);
}
