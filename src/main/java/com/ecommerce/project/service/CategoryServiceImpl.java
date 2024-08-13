package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.ICategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Implementatie van de service laag door gebruik te maken van de category interface. Hierin bevindt zich alle business
// logic (aka alle functionaliteit wat betreft de category)
@Service
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private ICategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories() {
        // Haal alle categories op
        List<Category> categories = categoryRepository.findAll();

        // Controleer of er iets in de lijst zit
        if (categories.isEmpty()) throw new APIException("No categories found.");

        // De lijst moet omgezet worden in een stream zodat de modelmapper de category kan omzetten naar een CategoryDTO
        // Deze DTO's worden bewaard in een nieuwe lijst.
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        // De controller verwacht een CategoryResponse terug. Deze moet een lijst bevatten met category DTOs
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);

        return categoryResponse;
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
