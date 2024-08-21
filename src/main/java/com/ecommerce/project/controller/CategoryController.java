package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.category.CategoryDTO;
import com.ecommerce.project.payload.category.CategoryResponse;
import com.ecommerce.project.service.ICategoryService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private ModelMapper modelMapper;

    // Default page number en page size meegegeven zodat de eerste pagina iig opgehaald kan worden zonder dat er request
    // parameters meegegeven worden
    @GetMapping("public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR) String sortDir

    ) {
        CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    // De @Valid annotation controleert of de constraints die gezet zijn in het object worden nageleefd. Als dat niet
    // het geval is. Dan wordt er gekeken waarom de constraints niet zijn nageleefd en wordt de juiste status code terug
    // gegeven.
    @PostMapping("public/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        // Sla de status op bij een succes en toon dit aan de end-user
        CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId) {
        CategoryDTO deleteCategoryDTO = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(deleteCategoryDTO, HttpStatus.OK);
    }

    @PutMapping("public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @PathVariable Long categoryId,
                                                      @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updatedCategoryDTO = categoryService.updateCategory(categoryId, categoryDTO);
        return new ResponseEntity<>(updatedCategoryDTO, HttpStatus.OK);
    }
}
