package com.eCommerce.product.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.product.model.request.CategoryRequest;
import com.eCommerce.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> creatCategory(
            @RequestBody @Valid CategoryRequest request
    ) {
        categoryService.createCategory(request);
        return ResponseEntity.ok(Response.ofSucceeded(HttpStatus.CREATED));
    }


}
