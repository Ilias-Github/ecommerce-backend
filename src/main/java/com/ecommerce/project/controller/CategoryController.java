package com.ecommerce.project.controller;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.service.ICategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Geeft bij Spring Boot aan dat het een Controller betreft en dat alle endpoints dezelfde pattern hebben
//
// De try-catches zorgen ervoor dat de juiste status codes en messages worden gegenereerd.
@RestController
@RequestMapping("api/")
public class CategoryController {

    // Spring maakt gebruik van dependency injection bij runtime waarbij de juiste bean wordt geïnjecteerd in het
    // attribuut. De interface zorgt voor loose coupling waardoor meerdere implementaties mogelijk zijn en minimale
    // aanpassingen gemaakt hoeven te worden in de code.
    @Autowired
    private ICategoryService categoryService;

    @GetMapping("public/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> allCategories = categoryService.getAllCategories();
        try {
            return new ResponseEntity<>(allCategories, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(allCategories, e.getStatusCode());
        }
    }

    // De @Valid annotation controleert of de constraints die gezet zijn in het object worden nageleefd. Als dat niet
    // het geval is. Dan wordt er gekeken waarom de constraints niet zijn nageleefd en wordt de juiste status code terug
    // gegeven.
    @PostMapping("public/categories")
    public ResponseEntity<String> CreateCategory(@Valid @RequestBody Category category) {
        try {
            // Sla de status op bij een succes en toon dit aan de end user
            String status = categoryService.createCategory(category);
            return new ResponseEntity<>(status, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @DeleteMapping("public/categories/{categoryId}")
    public ResponseEntity<String> DeleteCategory(@PathVariable Long categoryId) {
        try {
            String status = categoryService.deleteCategory(categoryId);
            return new ResponseEntity<>(status, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @PutMapping("public/categories/{categoryId}")
    public ResponseEntity<String> UpdateCategory(@PathVariable Long categoryId, @RequestBody Category category) {
        try {
            String status = categoryService.updateCategory(categoryId, category.getCategoryName());
            return new ResponseEntity<>(status, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }
}
