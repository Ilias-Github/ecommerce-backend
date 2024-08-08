package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;

import java.util.List;

public interface ICategoryService {
    List<Category> getAllCategories();
    String createCategory(Category category);
    String deleteCategory(Long categoryId);
}
