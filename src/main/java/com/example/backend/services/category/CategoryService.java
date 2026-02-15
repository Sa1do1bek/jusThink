package com.example.backend.services.category;

import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Category;
import com.example.backend.repositories.CategoryRepository;
import com.example.backend.requests.CreateCategoryRequest;
import com.example.backend.requests.UpdateCategoryRequest;
import com.example.backend.responses.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService{

    private final CategoryRepository categoryRepository;

    @Override
    public Category createCategory(CreateCategoryRequest request) {
        Category category = new Category();

        if (request.name().length() < 3)
            throw new IllegalActionException("Category name must contain at least 3 characters!");

        category.setName(request.name());

        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(UUID categoryId, UpdateCategoryRequest request) {
        Category oldCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

        if (request.name().length() < 3)
            throw new IllegalActionException("Category name must contain at least 3 characters!");

        oldCategory.setName(request.name());

        return categoryRepository.save(oldCategory);
    }

    @Override
    public void deleteCategory(UUID id) {
        Category oldCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

        categoryRepository.delete(oldCategory);
    }

    @Override
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public CategoryResponse convertCategoryToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}