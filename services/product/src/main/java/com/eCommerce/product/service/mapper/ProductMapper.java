package com.eCommerce.product.service.mapper;

import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.entity.Category;
import com.eCommerce.product.model.entity.Product;
import com.eCommerce.product.model.enumn.ProductStatus;
import com.eCommerce.product.model.projection.ProductListProjection;
import com.eCommerce.product.model.projection.RelatedProductProjection;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.response.ProductPurchaseResponse;
import com.eCommerce.product.model.response.ProductResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Central place for translating between API DTOs, projections, and entities.
 * Keeps discount/price math in one place to avoid divergence between endpoints.
 */
@Service
public class ProductMapper {

    /**
     * Build a Product entity from a request + its resolved Category.
     * Rating defaults to zero here; real systems may hydrate from reviews service.
     */
    public Product toEntity(ProductRequest request, Category category) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPercent(request.getDiscountPercent())
                .availableQuantity(request.getAvailableQuantity())
                .imageUrl(request.getImageUrl())
                .brand(request.getBrand())
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .isNew(Boolean.TRUE.equals(request.getIsNew()))
                .rating(0.0)
                .ratingCount(0)
                .category(category)
                .build();
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        BigDecimal finalPrice = calculateFinalPrice(product.getPrice(), product.getDiscountPercent());

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .price(product.getPrice())
                .finalPrice(finalPrice)
                .discountPercent(product.getDiscountPercent())
                .availableQuantity(product.getAvailableQuantity())
                .inStock(isInStock(product.getAvailableQuantity()))
                .imageUrl(product.getImageUrl())
                .brand(product.getBrand())
                .rating(product.getRating())
                .ratingCount(product.getRatingCount())
                .isFeatured(product.getIsFeatured())
                .isNew(product.getIsNew())
                .status(product.getStatus())
                .category(CategoryMapper.toDto(product.getCategory()))
                .build();
    }

    /**
     * Maps projection used for search/list endpoints.
     * Avoids loading entire entity graph for performance.
     */
    public ProductResponse mapToProductResponse(ProductListProjection projection) {
        if (projection == null) {
            return null;
        }

        BigDecimal finalPrice = calculateFinalPrice(projection.getPrice(), projection.getDiscountPercent());
        boolean inStock = isInStock(projection.getAvailableQuantity());

        CategoryDto categoryDto = Optional.ofNullable(projection.getCategoryId())
                .map(id -> CategoryDto.builder()
                        .id(id)
                        .name(projection.getCategoryName())
                        .description(projection.getCategoryDescription())
                        .slug(projection.getCategorySlug())
                        .imageUrl(projection.getCategoryImageUrl())
                        .icon(projection.getCategoryIcon())
                        .isActive(projection.getCategoryIsActive())
                        .build())
                .orElse(null);

        return ProductResponse.builder()
                .id(projection.getId())
                .sku(projection.getSku())
                .name(projection.getName())
                .shortDescription(projection.getShortDescription())
                .description(projection.getDescription())
                .price(projection.getPrice())
                .finalPrice(finalPrice)
                .discountPercent(projection.getDiscountPercent())
                .availableQuantity(projection.getAvailableQuantity())
                .inStock(inStock)
                .imageUrl(projection.getImageUrl())
                .brand(projection.getBrand())
                .rating(projection.getRating())
                .ratingCount(projection.getRatingCount())
                .isFeatured(projection.getIsFeatured())
                .isNew(projection.getIsNew())
                .category(categoryDto)
                .build();
    }

    public ProductPurchaseResponse toPurchaseResponse(Product product, Integer quantity) {
        if (product == null || quantity == null) {
            return null;
        }

        BigDecimal finalPrice = calculateFinalPrice(product.getPrice(), product.getDiscountPercent());
        BigDecimal totalPrice = finalPrice != null
                ? finalPrice.multiply(BigDecimal.valueOf(quantity))
                : null;

        return ProductPurchaseResponse.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .price(product.getPrice())
                .finalPrice(finalPrice)
                .discountPercent(product.getDiscountPercent())
                .quantity(quantity)
                .availableQuantity(product.getAvailableQuantity())
                .totalPrice(totalPrice)
                .imageUrl(product.getImageUrl())
                .build();
    }

    public ProductResponse fromRelatedProjection(RelatedProductProjection projection) {
        if (projection == null) {
            return null;
        }

        BigDecimal finalPrice = calculateFinalPrice(projection.getPrice(), projection.getDiscountPercent());
        boolean inStock = isInStock(projection.getAvailableQuantity());

        CategoryDto category = CategoryDto.builder()
                .id(projection.getCategoryId())
                .name(projection.getCategoryName())
                .slug(projection.getCategorySlug())
                .imageUrl(projection.getCategoryImageUrl())
                .icon(projection.getCategoryIcon())
                .build();

        return ProductResponse.builder()
                .id(projection.getId())
                .sku(projection.getSku())
                .name(projection.getName())
                .shortDescription(projection.getShortDescription())
                .description(projection.getDescription())
                .price(projection.getPrice())
                .finalPrice(finalPrice)
                .discountPercent(projection.getDiscountPercent())
                .availableQuantity(projection.getAvailableQuantity())
                .inStock(inStock)
                .imageUrl(projection.getImageUrl())
                .brand(projection.getBrand())
                .rating(projection.getRating())
                .ratingCount(projection.getRatingCount())
                .isFeatured(projection.getIsFeatured())
                .isNew(projection.getIsNew())
                // Related products are filtered to ACTIVE in the query
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private BigDecimal calculateFinalPrice(BigDecimal price, Integer discountPercent) {
        if (price == null) {
            return null;
        }
        if (discountPercent == null || discountPercent <= 0) {
            return price;
        }

        BigDecimal discount = price
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return price.subtract(discount);
    }

    private boolean isInStock(Integer availableQuantity) {
        return availableQuantity != null && availableQuantity > 0;
    }
}
