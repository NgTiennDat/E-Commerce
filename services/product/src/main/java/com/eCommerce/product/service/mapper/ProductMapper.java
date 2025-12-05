package com.eCommerce.product.service.mapper;

import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.entity.Category;
import com.eCommerce.product.model.entity.Product;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.response.ProductPurchaseResponse;
import com.eCommerce.product.model.response.ProductResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProductMapper {

    /**
     * Map ProductRequest + Category -> Product entity (dùng cho create/update)
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
                // rating/ratingCount có thể default ở entity hoặc set ở đây
                .rating(0.0)
                .ratingCount(0)
                .category(category)
                .build();
    }

    /**
     * Map Product entity -> ProductResponse (dùng cho list + detail)
     */
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        BigDecimal finalPrice = calculateFinalPrice(product);

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
                .inStock(product.getAvailableQuantity() != null && product.getAvailableQuantity() > 0)
                .imageUrl(product.getImageUrl())
                .brand(product.getBrand())
                .rating(product.getRating())
                .ratingCount(product.getRatingCount())
                .isFeatured(product.getIsFeatured())
                .isNew(product.getIsNew())
                .category(toCategoryDto(product.getCategory()))
                .build();
    }

    /**
     * Map Product entity + quantity -> ProductPurchaseResponse (flow purchase)
     */
    public ProductPurchaseResponse toPurchaseResponse(Product product, Integer quantity) {
        if (product == null || quantity == null) {
            return null;
        }

        BigDecimal finalPrice = calculateFinalPrice(product);
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

    // ================== private helpers ==================

    /**
     * Tính finalPrice = price - (price * discountPercent / 100)
     * Nếu không có discountPercent thì trả về price.
     */
    private BigDecimal calculateFinalPrice(Product product) {
        if (product.getPrice() == null) {
            return null;
        }

        Integer discountPercent = product.getDiscountPercent();
        if (discountPercent == null || discountPercent <= 0) {
            return product.getPrice();
        }

        BigDecimal discount = product.getPrice()
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return product.getPrice().subtract(discount);
    }

    /**
     * Map Category entity -> CategoryDto (dùng trong ProductResponse).
     */
    private CategoryDto toCategoryDto(Category category) {
        return CategoryMapper.toDto(category);
    }
}
