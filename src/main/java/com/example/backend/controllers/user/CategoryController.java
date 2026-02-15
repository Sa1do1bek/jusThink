package com.example.backend.controllers.user;

import com.example.backend.requests.CreateCategoryRequest;
import com.example.backend.requests.UpdateCategoryRequest;
import com.example.backend.responses.ApiResponse;
import com.example.backend.services.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasAuthority('category:create')")
    @PostMapping
    public ResponseEntity<ApiResponse> createCategory(@RequestBody CreateCategoryRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Category created successfully!",
                    categoryService.convertCategoryToResponse(categoryService.createCategory(request))
            ));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('category:update')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable UUID categoryId, @RequestBody UpdateCategoryRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Category updated successfully!",
                    categoryService.convertCategoryToResponse(categoryService.updateCategory(categoryId, request))
            ));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('category:delete')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable UUID categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok(new ApiResponse("Category deleted successfully!", null));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('category-by-id:get')")
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable UUID categoryId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Category found!",
                    categoryService.convertCategoryToResponse(categoryService.getCategoryById(categoryId))
            ));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('category-all:get')")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategories() {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Category found!",
                    categoryService.getAllCategories().stream().map(categoryService::convertCategoryToResponse).toList()
            ));
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private ResponseEntity<ApiResponse> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiResponse(message, null));
    }
}