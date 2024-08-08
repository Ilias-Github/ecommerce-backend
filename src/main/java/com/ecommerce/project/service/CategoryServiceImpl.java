package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {
    private List<Category> categories = new ArrayList<>();
    private Long nextId = 1L;

    @Override
    public List<Category> getAllCategories() {
        return categories;
    }

    @Override
    public String createCategory(Category category) {
        category.setCategoryId(nextId++);
        categories.add(category);
        return "Successfully created category " + category.getCategoryName();
    }

    @Override
    public String deleteCategory(Long categoryId) {
        // Om een categorie te verwijderen moet eerst uitgezocht worden welk object in de lijst verwijderd dient te worden
        // Verwijderen aan de hand van alleen het id werkt niet
        Category category = categories
                .stream()
                .filter(c -> c.getCategoryId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        categories.remove(category);
        return "Successfully removed category " + category.getCategoryName();
    }
}
