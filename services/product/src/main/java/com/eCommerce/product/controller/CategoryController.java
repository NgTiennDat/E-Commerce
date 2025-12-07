package com.eCommerce.product.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

//    @GetMapping
//    public ResponseEntity<?> getCategories() {
//        return ResponseEntity.of(Response.ofSucceeded(categoryService.getAllCategories()));
//    }
}
