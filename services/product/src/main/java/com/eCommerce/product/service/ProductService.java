package com.eCommerce.product.service;

import com.eCommerce.product.model.enumn.ProductStatus;
import com.eCommerce.product.model.request.ProductPurchaseRequest;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.response.ProductPurchaseResponse;
import com.eCommerce.product.model.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    Long addProduct(ProductRequest request);

    List<ProductPurchaseResponse> purchaseProduct(List<ProductPurchaseRequest> request);

    ProductResponse getProductDetail(Long productId);

    Page<ProductResponse> getAllProductInCategory(Long categoryId, int page, int size);

    Page<ProductResponse> getProducts(
            int page,
            int size,
            String keyword,
            Long categoryId,
            ProductStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String brand,
            Boolean isFeatured,
            Boolean isNew
    );

    void updateProduct(Long productId, ProductRequest request);

    void deleteProduct(Long productId);

    List<ProductResponse> getRelatedProducts(Long productId, int limit);
}
