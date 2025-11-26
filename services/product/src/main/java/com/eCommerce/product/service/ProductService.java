package com.eCommerce.product.service;

import com.eCommerce.product.model.request.ProductPurchaseRequest;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.response.ProductPurchaseResponse;
import com.eCommerce.product.model.response.ProductResponse;

import java.util.List;

public interface ProductService {

    Integer addProduct(ProductRequest request);

    List<ProductPurchaseResponse> purchaseProduct(List<ProductPurchaseRequest> request);

    ProductResponse getProductDetail(Integer productId);

    List<ProductResponse> getAllProductInCategory(Integer categoryId);

    List<ProductResponse> getAllProducts();

}
