package com.eCommerce.product.controller;

import com.eCommerce.product.model.request.ProductPurchaseRequest;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.response.ProductPurchaseResponse;
import com.eCommerce.product.model.response.ProductResponse;
import com.eCommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/add-product")
    public ResponseEntity<Integer> addProduct(
            @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.addProduct(request));
    }

    @PostMapping("/purchase")
    public ResponseEntity<List<ProductPurchaseResponse>> purchaseProducts(
            @RequestBody List<ProductPurchaseRequest> request
    ) {
        return ResponseEntity.ok(productService.purchaseProduct(request));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductDetail(
            @PathVariable("productId") Integer productId
    ) {
        return ResponseEntity.ok(productService.getProductDetail(productId));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getAllProductInCategory(
            @PathVariable("categoryId") Integer categoryId
    ) {
        return ResponseEntity.ok(productService.getAllProductInCategory(categoryId));
    }

    @GetMapping("/all-product")
    public ResponseEntity<List<ProductResponse>> getAllProduct() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
