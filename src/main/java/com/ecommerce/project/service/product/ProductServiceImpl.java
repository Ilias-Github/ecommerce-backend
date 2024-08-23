package com.ecommerce.project.service.product;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.product.ProductDTO;
import com.ecommerce.project.payload.product.ProductResponse;
import com.ecommerce.project.repositories.ICategoryRepository;
import com.ecommerce.project.repositories.IProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {
    @Autowired
    private IProductRepository productRepository;
    @Autowired
    private ICategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductResponse getAllProducts() {
        List<Product> products = productRepository.findAll();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);

        return productResponse;
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO, Long categoryId) {
        Product product = productRepository.findByProductName(productDTO.getProductName());
        if (product != null) {
            throw new APIException("Product with the name '" + productDTO.getProductName() + "' already exists");
        }

        product = modelMapper.map(productDTO, Product.class);

        // Door de .orElseThrow() methode is het mogelijk om category op te slaan zonder er een optional van te maken.
        // Optional geeft aan dat het kan zijn dat het object niet bestaat
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        product.setCategory(category);

        double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
        product.setSpecialPrice(specialPrice);

        product = productRepository.save(product);

        return modelMapper.map(product, ProductDTO.class);
    }
}
