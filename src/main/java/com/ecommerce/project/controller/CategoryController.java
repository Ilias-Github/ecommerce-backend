package com.ecommerce.project.controller;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.service.ICategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Geeft bij Spring Boot aan dat het een Controller betreft en dat alle endpoints dezelfde pattern hebben
// De controller moet zo simpel mogelijk zijn. Dus geen checks of error handling, dat is onderdeel van de service class.
// Daarom geen try/catch in controllers plaatsen
// Alleen de succes messages worden doorgegeven aan de front-end. De error messages worden vanuit de business logic
// gegeven
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
        return new ResponseEntity<>(allCategories, HttpStatus.OK);
    }

    // De @Valid annotation controleert of de constraints die gezet zijn in het object worden nageleefd. Als dat niet
    // het geval is. Dan wordt er gekeken waarom de constraints niet zijn nageleefd en wordt de juiste status code terug
    // gegeven.
    @PostMapping("public/categories")
    public ResponseEntity<String> CreateCategory(@Valid @RequestBody Category category) {
        // Sla de status op bij een succes en toon dit aan de end-user
        String status = categoryService.createCategory(category);
        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @DeleteMapping("public/categories/{categoryId}")
    public ResponseEntity<String> DeleteCategory(@PathVariable Long categoryId) {
        String status = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PutMapping("public/categories/{categoryId}")
    public ResponseEntity<String> UpdateCategory(@Valid @PathVariable Long categoryId, @RequestBody Category category) {
        String status = categoryService.updateCategory(categoryId, category.getCategoryName());
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
