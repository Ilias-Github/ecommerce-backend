package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repositories.ICategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Implementatie van de service laag door gebruik te maken van de category interface. Hierin bevindt zich alle business
// logic (aka alle functionaliteit wat betreft de category)
@Service
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private ICategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) throw new APIException("No categories found.");

        return categories;
    }

    @Override
    public String createCategory(Category category) {
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());

        if (savedCategory != null) {
            throw new APIException("Category with the name '" + category.getCategoryName() + "' already exists");
        }
        categoryRepository.save(category);
        return "Successfully created category " + category.getCategoryName();
    }

    @Override
    public String deleteCategory(Long categoryId) {
        // Controleer of deze category bestaat, anders exception
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Verwijder de category gebasseerd op het meegegeven ID
        categoryRepository.deleteById(categoryId);
        return "Successfully removed category with ID " + categoryId;
    }

    @Override
    public String updateCategory(Long categoryId, String newCategoryName) {
        // Controleer of deze category bestaat, anders exception
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Zet de category name van de categorie die opgehaald is
        category.setCategoryName(newCategoryName);

        // Sla deze geupdatet category op in de database
        categoryRepository.save(category);
        return "Successfully updated category with ID " + categoryId + " to " + newCategoryName;
    }
}
