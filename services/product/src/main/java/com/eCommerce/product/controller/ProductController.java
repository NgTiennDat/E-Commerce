package com.eCommerce.product.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.product.model.request.ProductPurchaseRequest;
import com.eCommerce.product.model.request.ProductSearchRequest;
import com.eCommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseProducts(
            @RequestBody List<ProductPurchaseRequest> request
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.purchaseProduct(request)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetail(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.getProductDetail(productId)));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getAllProductInCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.getAllProductInCategory(categoryId, page, size)));
    }

    @GetMapping
    public ResponseEntity<?> getProducts(
            @ParameterObject ProductSearchRequest request
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.getProducts(request)));
    }

    @GetMapping("/{productId}/related")
    public ResponseEntity<?> getRelatedProducts(
            @PathVariable Long productId,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.getRelatedProducts(productId, limit)));
    }

}
