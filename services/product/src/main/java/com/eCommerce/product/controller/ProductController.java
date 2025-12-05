package com.eCommerce.product.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.product.model.request.ProductPurchaseRequest;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.response.ProductPurchaseResponse;
import com.eCommerce.product.model.response.ProductResponse;
import com.eCommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/add-product")
    public ResponseEntity<?> addProduct(
            @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.addProduct(request)));
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseProducts(
            @RequestBody List<ProductPurchaseRequest> request
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.purchaseProduct(request)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetail(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.getProductDetail(productId)));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getAllProductInCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.getAllProductInCategory(categoryId, page, size)));
    }

    @GetMapping("/all-product")
    public ResponseEntity<?> getAllProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.getAllProducts(page, size)));
    }
}
