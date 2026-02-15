package com.example.backend.services.category;

import com.example.backend.models.Category;
import com.example.backend.requests.CreateCategoryRequest;
import com.example.backend.requests.UpdateCategoryRequest;
import com.example.backend.responses.CategoryResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICategoryService {
    Category createCategory(CreateCategoryRequest request);
    Category updateCategory(UUID categoryId, UpdateCategoryRequest request);
    void deleteCategory(UUID id);
    Category getCategoryById(UUID id);
    List<Category> getAllCategories();
    CategoryResponse convertCategoryToResponse(Category category);
}
