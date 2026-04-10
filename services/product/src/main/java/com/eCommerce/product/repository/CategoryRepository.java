package com.eCommerce.product.repository;

import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.entity.Category;
import com.eCommerce.product.model.projection.CategoryListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndIsDeletedFalse(Long id);

    boolean existsBySlugAndIsDeletedFalse(String slug);

    @Query(
            value = """
        SELECT
            c.id               AS id,
            c.name             AS name,
            c.slug             AS slug,
            c.is_active        AS isActive,
            c.parent_id        AS parentId,
            CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM category child
                    WHERE child.is_deleted = b'0'
                      AND child.parent_id = c.id
                ) THEN TRUE
                ELSE FALSE
            END                AS hasChildren
        FROM category c
        WHERE c.is_deleted = b'0'
          AND (:isActive IS NULL OR c.is_active = IF(:isActive, b'1', b'0'))
          AND (
                :keyword IS NULL OR :keyword = ''
                OR LOWER(c.name) LIKE CONCAT('%', LOWER(:keyword), '%')
                OR LOWER(c.slug) LIKE CONCAT('%', LOWER(:keyword), '%')
              )
          AND (
                :parentId IS NULL
                OR c.parent_id = :parentId
              )
          AND (
                :hasChildren IS NULL
                OR (
                    :hasChildren = TRUE AND EXISTS (
                        SELECT 1
                        FROM category child2
                        WHERE child2.is_deleted = b'0'
                          AND child2.parent_id = c.id
                    )
                )
                OR (
                    :hasChildren = FALSE AND NOT EXISTS (
                        SELECT 1
                        FROM category child3
                        WHERE child3.is_deleted = b'0'
                          AND child3.parent_id = c.id
                    )
                )
              )
        ORDER BY c.id DESC
        """,
            countQuery = """
        SELECT COUNT(1)
        FROM category c
        WHERE c.is_deleted = b'0'
          AND (:isActive IS NULL OR c.is_active = IF(:isActive, b'1', b'0'))
          AND (
                :keyword IS NULL OR :keyword = ''
                OR LOWER(c.name) LIKE CONCAT('%', LOWER(:keyword), '%')
                OR LOWER(c.slug) LIKE CONCAT('%', LOWER(:keyword), '%')
              )
          AND (
                :parentId IS NULL
                OR c.parent_id = :parentId
              )
          AND (
                :hasChildren IS NULL
                OR (
                    :hasChildren = TRUE AND EXISTS (
                        SELECT 1
                        FROM category child2
                        WHERE child2.is_deleted = b'0'
                          AND child2.parent_id = c.id
                    )
                )
                OR (
                    :hasChildren = FALSE AND NOT EXISTS (
                        SELECT 1
                        FROM category child3
                        WHERE child3.is_deleted = b'0'
                          AND child3.parent_id = c.id
                    )
                )
              )
        """,
            nativeQuery = true
    )
    Page<CategoryListProjection> searchCategories(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            @Param("parentId") Long parentId,
            @Param("hasChildren") Boolean hasChildren,
            Pageable pageable
    );

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
