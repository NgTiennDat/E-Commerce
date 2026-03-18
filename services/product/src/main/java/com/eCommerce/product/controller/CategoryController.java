package com.eCommerce.product.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.product.model.request.CategorySearchRequest;
import com.eCommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     *
     */
    @GetMapping
    public ResponseEntity<?> searchCategories(
            @ParameterObject CategorySearchRequest request
    ) {
        return ResponseEntity.ok(
                Response.ofSucceeded(categoryService.getCategories(request))
        );
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryDetail(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(categoryService.getCategoryDetail(categoryId)));
    }

    @GetMapping("/slug")
    public ResponseEntity<?> getCategorySlug(
            @RequestParam("slug") String slug
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(categoryService.getCategoryBySlug(slug)));
    }
}
