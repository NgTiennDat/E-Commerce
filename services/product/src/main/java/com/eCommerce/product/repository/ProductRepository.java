package com.eCommerce.product.repository;

import com.eCommerce.product.model.entity.Product;
import com.eCommerce.product.model.enumn.ProductStatus;
import com.eCommerce.product.model.projection.ProductListProjection;
import com.eCommerce.product.model.projection.RelatedProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIdInOrderById(List<Long> Ids);

    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByCategoryIdAndIdNotAndStatus(
            Long categoryId, Long productId, ProductStatus productStatus, Pageable pageable
    );

    @Query(
            value = """
            SELECT
                p.id                       AS id,
                p.sku                      AS sku,
                p.name                     AS name,
                p.short_description        AS shortDescription,
                p.description              AS description,
                p.price                    AS price,
                p.discount_percent         AS discountPercent,
                p.available_quantity       AS availableQuantity,
                p.image_url                AS imageUrl,
                p.brand                    AS brand,
                p.rating                   AS rating,
                p.rating_count             AS ratingCount,
                p.is_featured              AS isFeatured,
                p.is_new                   AS isNew,
                c.id                       AS categoryId,
                c.name                     AS categoryName,
                c.slug                     AS categorySlug,
                c.id                       AS categoryId,
                c.name                     AS categoryName,
                c.description              AS categoryDescription,
                c.slug                     AS categorySlug,
                c.image_url                AS categoryImageUrl,
                c.icon                     AS categoryIcon,
                c.is_active                AS categoryIsActive
            FROM products p
            LEFT JOIN category c ON p.category_id = c.id
            WHERE
                p.is_deleted = FALSE
                AND (:keyword IS NULL OR
                     LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                     OR LOWER(p.short_description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                     OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (:categoryId IS NULL OR p.category_id = :categoryId)
                AND (:status IS NULL OR p.status = :status)
                AND (:minPrice IS NULL OR p.price >= :minPrice)
                AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                AND (:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
                AND (:isFeatured IS NULL OR p.is_featured = :isFeatured)
                AND (:isNew IS NULL OR p.is_new = :isNew)
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products p
            LEFT JOIN category c ON p.category_id = c.id
            WHERE
                p.is_deleted = FALSE
                AND (:keyword IS NULL OR
                     LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                     OR LOWER(p.short_description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                     OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (:categoryId IS NULL OR p.category_id = :categoryId)
                AND (:status IS NULL OR p.status = :status)
                AND (:minPrice IS NULL OR p.price >= :minPrice)
                AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                AND (:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
                AND (:isFeatured IS NULL OR p.is_featured = :isFeatured)
                AND (:isNew IS NULL OR p.is_new = :isNew)
            """,
            nativeQuery = true
    )
    Page<ProductListProjection> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("brand") String brand,
            @Param("isFeatured") Boolean isFeatured,
            @Param("isNew") Boolean isNew,
            Pageable pageable
    );

    @Query(
            value = """
            SELECT 
                p.id                            AS id,
                p.sku                           AS sku,
                p.name                          AS name,
                p.short_description             AS shortDescription,
                p.description                   AS description,
                p.price                         AS price,
                p.discount_percent              AS discountPercent,
                p.available_quantity            AS availableQuantity,
                p.image_url                     AS imageUrl,
                p.brand                         AS brand,
                p.rating                        AS rating,
                p.rating_count                  AS ratingCount,
                p.is_featured                   AS isFeatured,
                p.is_new                        AS isNew,
                 p.status                       AS status,
                c.id                            AS categoryId,
                c.name                          AS categoryName,
                c.slug                          AS categorySlug,
                c.image_url                     AS categoryImageUrl,
                c.icon                          AS categoryIcon
            FROM products p
            JOIN category c ON p.category_id = c.id
            WHERE 
                p.category_id = :categoryId
                AND p.id <> :productId
                AND p.status = 'ACTIVE'
                AND p.is_deleted = b'0'
                AND c.is_deleted = b'0'
            ORDER BY 
                p.is_featured DESC,
                p.rating DESC
            LIMIT :limit
            """,
            nativeQuery = true
    )
    List<RelatedProductProjection> findRelatedProductsNative(
            @Param("categoryId") Long categoryId,
            @Param("productId") Long productId,
            @Param("limit") int limit
    );
}
