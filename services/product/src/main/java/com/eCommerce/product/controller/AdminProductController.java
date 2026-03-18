package com.eCommerce.product.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.product.model.enumn.ProductStatus;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @PostMapping("/add-product")
    public ResponseEntity<?> addProduct(
            @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.addProduct(request)));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("productId") Long productId,
            @RequestBody ProductRequest request
    ) {
        productService.updateProduct(productId, request);
        return ResponseEntity.ok(Response.ofSucceeded(HttpStatus.OK));
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<?> updateProductStatus(
            @PathVariable Long productId,
            @RequestParam ProductStatus status
    ) {
        productService.updateProductStatus(productId, status);
        return ResponseEntity.ok(Response.ofSucceeded(HttpStatus.OK));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable("productId") Long productId
    ) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(Response.ofSucceeded(HttpStatus.OK));
    }

}
