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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

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
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Vind het product waaraan de afbeelding toegevoegd dient te worden
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Path waar de plaatjes opgeslagen worden op de server. Dit is op de root van de server. Een directory die
        // naast de src directory bestaat
        String path = "images";

        // Sla de bestandsnaam op die teruggegeven wordt na het succesvol uploaden van een image
        String fileName = uploadImage(path, image);

        // Sla de naam van de afbeelding op in het object
        product.setImage(fileName);

        Product updatedProduct = productRepository.save(product);

        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    private String uploadImage(String path, MultipartFile file) throws IOException {
        // De bestandsnaam is nodig om te achterhalen welke extensie de file heeft
        String originalFileName = file.getOriginalFilename();

        // Genereer een random unieke file naam zodat deze geen files in de database gaat overschrijven
        // UUID genereert een random string die zo lang en random is dat het vrijwel onmogelijk is dat het twee keer
        // dezelfde string genereert. Omdat de kans op duplicate namen extreem klein is, is het niet nodig om te
        // checken of de naam al bestaat
        String randomId = UUID.randomUUID().toString();
        // Hier wordt de extensie van de originele filenaam toegevoegd aan de randomId. De extensie is altijd de
        // laatste characters na de laatste "." in een filenaam
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        // File.separator wordt gebruikt omdat separators OS afhankelijk zijn. Deze wil je dus nooit hard coded hebben
        String filePath = path + File.separator + fileName;

        // Check of de directory bestaan. Maak deze aan indien het niet het geval is
        File folder = new File(path);
        if (!folder.exists())
            folder.mkdir();

        // Kopieer het bestand naar de door ons aangegeven file path
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }

    private double SpecialPriceCalculation(double price, double discount) {
        double specialPrice = price - (discount * 0.01) * price;

        return round(specialPrice);
    }
}
