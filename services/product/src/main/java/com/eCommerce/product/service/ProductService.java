package com.eCommerce.product.service;

import com.eCommerce.product.model.request.ProductPurchaseRequest;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.response.ProductPurchaseResponse;
import com.eCommerce.product.model.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    Long addProduct(ProductRequest request);

    List<ProductPurchaseResponse> purchaseProduct(List<ProductPurchaseRequest> request);

    ProductResponse getProductDetail(Long productId);

    Page<ProductResponse> getAllProductInCategory(Long categoryId, int page, int size);

    Page<ProductResponse> getAllProducts(int page, int size);
}
