package com.ecommerce.project.service;

import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;

// Beschrijf alle methods die je nodig hebt voor de implementatie in de service layer
// De reden voor de Interface is om loose coupling te bereiken tussen de controller en service laag.
// Dit betekent dat de developer een compleet nieuwe service kan schrijven met een andere naam en deze kan injecteren
// overal waar de interface wordt gebruikt (in dit geval de controller). Hiervoor hoef je geen files in de service laag
// aan te passen.
public interface ICategoryService {
    CategoryDTO createCategory(CategoryDTO CategoryDTO);

    // CategoryResponse wordt teruggegeven omdat de client een lijst met DTO's moet terug krijgen
    CategoryResponse getAllCategories();

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);

    CategoryDTO deleteCategory(Long categoryId);
}
