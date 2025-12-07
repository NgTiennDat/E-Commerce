package com.eCommerce.product.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.product.model.enumn.ProductStatus;
import com.eCommerce.product.model.request.ProductPurchaseRequest;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.response.ProductResponse;
import com.eCommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
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

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getAllProductInCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.getAllProductInCategory(categoryId, page, size)));
    }

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) Boolean isNew
    ) {
        return ResponseEntity.ok(
                Response.ofSucceeded(
                        productService.getProducts(
                                page,
                                size,
                                keyword,
                                categoryName,
                                status,
                                minPrice,
                                maxPrice,
                                brand,
                                isFeatured,
                                isNew
                        )
                )
        );
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("productId") Long productId,
            @RequestBody ProductRequest request
    ) {
        productService.updateProduct(productId, request);
        return ResponseEntity.ok(Response.ofSucceeded(HttpStatus.OK));
    }

    @DeleteMapping("{productId}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable("productId") Long productId
    ) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(Response.ofSucceeded(HttpStatus.OK));
    }

    @GetMapping("/{productId}/related")
    public ResponseEntity<?> getRelatedProducts(
            @PathVariable Long productId,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(productService.getRelatedProducts(productId, limit)));
    }
}
