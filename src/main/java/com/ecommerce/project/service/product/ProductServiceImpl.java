package com.ecommerce.project.service.product;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.product.ProductDTO;
import com.ecommerce.project.payload.product.ProductResponse;
import com.ecommerce.project.repositories.ICartRepository;
import com.ecommerce.project.repositories.ICategoryRepository;
import com.ecommerce.project.repositories.IProductRepository;
import com.ecommerce.project.service.cart.ICartService;
import com.ecommerce.project.service.file.IFileService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static java.lang.Math.round;

@Service
public class ProductServiceImpl implements IProductService {
    @Autowired
    private IProductRepository productRepository;
    @Autowired
    private ICategoryRepository categoryRepository;
    @Autowired
    private ICartRepository cartRepository;
    @Autowired
    private ICartService cartService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private IFileService fileService;
    // Property van application.properties die globaal in het project gebruikt kan worden
    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO, Long categoryId) {
        Product product = productRepository.findByProductNameIgnoreCase(productDTO.getProductName());
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
    public ProductResponse getAllProducts(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productsPage = productRepository.findAll(pageDetails);
        List<Product> products = productsPage.getContent();

        if (products.isEmpty()) throw new APIException("Products don't exist yet");

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList());

        productResponse.setPageNumber(productsPage.getNumber());
        productResponse.setPageSize(productsPage.getSize());
        productResponse.setTotalElements(productsPage.getTotalElements());
        productResponse.setTotalPages(productsPage.getTotalPages());
        productResponse.setLastPage(productsPage.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(
            Long categoryId, int pageNumber, int pageSize, String sortBy, String sortOrder
    ) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> productsPage = productRepository.findByCategoryOrderByPriceAsc(category, pageable);

        if (productsPage.isEmpty())
            throw new APIException("No products found with the category " + category.getCategoryName());

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productsPage.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList());

        productResponse.setPageNumber(productsPage.getNumber());
        productResponse.setPageSize(productsPage.getSize());
        productResponse.setTotalElements(productsPage.getTotalElements());
        productResponse.setTotalPages(productsPage.getTotalPages());
        productResponse.setLastPage(productsPage.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(
            String productName, int pageNumber, int pageSize, String sortBy, String sortOrder
    ) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        // De % is bedoeld voor pattern matching. Als je deze niet toevoegt dan kan je alleen op exacte strings zoeken
        Page<Product> pageProducts = productRepository
                .findByProductNameLikeIgnoreCase('%' + productName + '%', pageable);

        if (pageProducts.isEmpty())
            throw new APIException("No products exist with the name '" + productName);

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(pageProducts.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList());

        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

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

        // Haal alle carts op die dit product bevat
        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        // Zet alle carts om naar cart dtos
        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            // Zet de cartItems van een cart om in een lijst van producten
            List<ProductDTO> products = cart.getCartItems()
                    .stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                    .toList();

            // Zet de lijst aan producten in de cartDTO
            cartDTO.setProducts(products);

            return cartDTO;
        }).toList();

        // Update elke cart met het geupdatet product
        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Voordat het product uit de database verdwijnt, moeten de carts die het product bevatten eerst updatet worden.
        // Anders krijg je carts met foreign keys die nergens naartoe leiden
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(productId));

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

        // Sla de bestandsnaam op die teruggegeven wordt na het succesvol uploaden van een image
        String fileName = fileService.uploadImage(path, image);

        // Sla de naam van de afbeelding op in het object
        product.setImage(fileName);

        Product updatedProduct = productRepository.save(product);

        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    private Page<Product> getProductsPage(String sortOrder, String sortBy, int pageNumber, int pageSize) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productsPage = productRepository.findAll(pageDetails);

        return productsPage;
    }

    private double SpecialPriceCalculation(double price, double discount) {
        double specialPrice = price - (discount * 0.01) * price;

        return round(specialPrice);
    }
}
