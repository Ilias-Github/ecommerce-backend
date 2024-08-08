package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.repositories.ICategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {
    private List<Category> categories = new ArrayList<>();
    private Long nextId = 1L;

    @Autowired
    private ICategoryRepository categoryRepository;
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public String createCategory(Category category) {
        categoryRepository.save(category);
        return "Successfully created category " + category.getCategoryName();
    }

    @Override
    public String deleteCategory(Long categoryId) {
        // Controleer of deze category bestaat, anders exception
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        categoryRepository.deleteById(categoryId);
        return "Successfully removed category with ID " + categoryId;
    }

    @Override
    public String updateCategory(Long categoryId, String newCategoryName) {
        List<Category> categories = categoryRepository.findAll();

        Category category = categories
                .stream()
                .filter(c -> c.getCategoryId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String oldCategoryName = category.getCategoryName();
        category.setCategoryName(newCategoryName);
        categoryRepository.save(category);

        return "Succesfully updated category " + oldCategoryName + " to " + category.getCategoryName();
    }
}
