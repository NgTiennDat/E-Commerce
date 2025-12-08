package com.eCommerce.product.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * @param isActive
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity<?> getCategories(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "true", required = false) Boolean isActive,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Boolean hasChildren
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(categoryService.getCategories(isActive)));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryDetail(
            @PathVariable("categoryId") Long categoryId
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
