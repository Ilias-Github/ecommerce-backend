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

import static java.lang.Math.round;

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
        if (product != null)
            throw new APIException("Product with the name '" + productDTO.getProductName() + "' already exists");

        product = modelMapper.map(productDTO, Product.class);

        // Door de .orElseThrow() methode is het mogelijk om category op te slaan zonder er een optional van te maken.
        // Optional geeft aan dat het kan zijn dat het object niet bestaat
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        product.setCategory(category);

        double specialPrice = SpecialPriceCalculation(product.getPrice(), product.getDiscount());
        product.setSpecialPrice(specialPrice);

        product = productRepository.save(product);

        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setQuantity(productDTO.getQuantity());
        product.setDiscount(productDTO.getDiscount());
        product.setPrice(productDTO.getPrice());

        double specialPrice = SpecialPriceCalculation(product.getPrice(), product.getDiscount());
        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        productRepository.deleteById(productId);

        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        List<Product> products = productRepository.findByCategoryOrderByPriceAsc(category);

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList());

        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String productName) {
        // De % is bedoeld voor pattern matching. Als je deze niet toevoegt dan kan je alleen op exacte strings zoeken
        List<Product> products = productRepository.findByProductNameLikeIgnoreCase('%' + productName + '%');

        if (products.isEmpty())
            throw new APIException("No products exist with the name '" + productName);

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList());

        return productResponse;
    }

    private double SpecialPriceCalculation(double price, double discount) {
        double specialPrice = price - (discount * 0.01) * price;

        return round(specialPrice);
    }
}
