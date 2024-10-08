package com.ecommerce.project.service.category;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.category.CategoryDTO;
import com.ecommerce.project.payload.category.CategoryResponse;
import com.ecommerce.project.repositories.ICategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public CategoryResponse getAllCategories(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        // TODO: uitgebreider uitleggen wat hier gebeurt
        // Bepalen in welke richting gesorteerd dient te worden en op welk veld
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Vraag een pagina op met de aangeleverde parameters
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        // Vind alle categories met de parameters meegegeven in de page details
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

        // Sla de opgevraagde pagina-informatie op in een lijst
        List<Category> categories = categoryPage.getContent();

        // Controleer of er iets in de lijst zit
        if (categories.isEmpty()) throw new APIException("No categories found.");

        // De lijst moet omgezet worden in een stream om manipulatie van de List<Category> makkelijker te maken door
        // gebruik te maken van de ingebouwde functionaliteit binnen Streams
        // Wij maken gebruik van twee verschillende, maar vergelijkbare modellen (Category en categoryDTO: qua structuur
        // en informatie is er veel overlap).
        // De .map method maakt het mogelijk om een object om te zetten naar iets anders.
        // modelMapper zorgt ervoor dat het ene model naar het ander model omgezet kan worden
        // Deze DTO's worden bewaard in een nieuwe lijst
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        // De controller verwacht een CategoryResponse terug. Deze verwacht een lijst aan CategoryDTOs
        CategoryResponse categoryResponse = new CategoryResponse();

        // Zet alle values van de category response
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());

        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Zet de DTO om in een Category object omdat de database een category object verwacht
        Category category = modelMapper.map(categoryDTO, Category.class);
        // Zoek de category op in de database
        Category dbCategory = categoryRepository.findByCategoryName(category.getCategoryName());

        // Als de category gevonden is, gooi een error omdat het niet mogelijk is om een category met dezelfde naam aan
        // te maken
        if (dbCategory != null) {
            throw new APIException("Category with the name '" + categoryDTO.getCategoryName() + "' already exists");
        }

        // Sla de category op in de database en sla het resultaat op zodat deze teruggegeven kan worden
        Category savedCategory = categoryRepository.save(category);

        // De client verwacht een DTO, dus convert de opgeslagen category naar een DTO
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        // Controleer of deze category bestaat, anders exception
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Verwijder de category gebaseerd op het meegegeven ID
        categoryRepository.deleteById(categoryId);

        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        String categoryName = categoryDTO.getCategoryName();

        // Controleer of deze category bestaat, anders exception
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Zet de category name van de categorie die opgehaald is
        category.setCategoryName(categoryName);

        // Sla deze geupdatet category op in de database
        Category updatedCategory = categoryRepository.save(category);
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }
}
