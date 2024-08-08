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
        // Haal alle categories zodat deze doorzocht kan worden
        List<Category> categories = categoryRepository.findAll();
        // Om een categorie te verwijderen moet eerst uitgezocht worden welk object in de lijst verwijderd dient te worden
        // Verwijderen aan de hand van alleen het id werkt niet
        Category category = categories
                .stream()
                .filter(c -> c.getCategoryId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        categoryRepository.delete(category);
        return "Successfully removed category " + category.getCategoryName();
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
