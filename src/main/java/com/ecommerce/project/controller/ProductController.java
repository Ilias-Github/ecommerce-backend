package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.product.ProductDTO;
import com.ecommerce.project.payload.product.ProductResponse;
import com.ecommerce.project.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/")
public class ProductController {
    @Autowired
    IProductService productService;

    @GetMapping("public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR) String sortDir
    ) {
        ProductResponse productResponse = productService.getAllProducts();
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PostMapping("public/products")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO, Long categoryId) {
        ProductDTO dto = productService.createProduct(productDTO, categoryId);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
}
