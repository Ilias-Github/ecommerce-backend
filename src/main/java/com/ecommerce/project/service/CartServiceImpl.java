package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.product.ProductDTO;
import com.ecommerce.project.repositories.ICartItemRepository;
import com.ecommerce.project.repositories.ICartRepository;
import com.ecommerce.project.repositories.IProductRepository;
import com.ecommerce.project.util.AuthUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private ICartRepository cartRepository;
    @Autowired
    private IProductRepository productRepository;
    @Autowired
    private ICartItemRepository cartItemRepository;
    @Autowired
    private AuthUtils authUtils;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            throw new APIException("No carts exist yet");
        }

        // De gevonden cart objects omzetten naar een CartDTO zodat deze teruggestuurd kan worden naar de client
        // TODO: map beter onderzoeken
        // Elke cart dient individueel behandelt te worden. Daarom moet deze gemapped worden
        // Map zorgt er ook voor dat een array omgezet kan worden naar een nieuwe array met minder code
        return carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            // De cartDTO verwacht een lijst aan ProductDTOs. Uit de database is een lijst aan Products opgehaald
            List<ProductDTO> products = cart.getCartItems()
                    .stream().map(product -> modelMapper.map(product.getProduct(), ProductDTO.class)).toList();

            cartDTO.setProducts(products);

            return cartDTO;
        }).toList();
    }

    @Override
    public CartDTO getUserCart() {
        // TODO: moet ik een cart aanmaken als deze niet bestaat?
        Cart cart = createCart();

        List<ProductDTO> productDTOS = cart.getCartItems().stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        cartDTO.setProducts(productDTOS);

        return cartDTO;
    }

    public CartDTO addProductToCart(Long productId, int quantity) {
        // Huidige cart ophalen van de ingelogde user
        Cart cart = createCart();

        // Haal het product op dat toegevoegd dient te worden aan de cart
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Check of het cartItem al bestaat
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getId(), productId);

        // TODO: update quantity als product al in de cart bestaat
        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (quantity > product.getQuantity()) {
            throw new APIException("Please, make an order of the " + product.getProductName() + " less than or equal to the quantity " + product.getQuantity());
        }

        // Creeer een cart item als de user het item nog niet in de cart heeft
        cartItem = new CartItem();

        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());
        cartItem.setCart(cart);

        cartItemRepository.save(cartItem);

        // TODO: dit is toch overbodig?
        // De quantity moet aangepast worden wanneer jet product daadwerkelijk gekocht wordt.
        // Wat hier gebeurt is echt onzinnig
        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> productDTOStream = cart.getCartItems().stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        }).toList();

        cartDTO.setProducts(productDTOStream);

        return cartDTO;
    }

    @Override
    public CartDTO updateCartQuantity(Long productId, int quantity) {
        Cart cart = createCart();

        // Vind het cartitem dat overeenkomt met het product
        CartItem cartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .findAny()
                .orElseThrow(() -> new APIException("Product not part of the list"));

        System.out.println("141");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() < quantity + cartItem.getQuantity()) {
            throw new APIException("Quantity exceeds available product quantity");
        }

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        // 1. Check of er genoeg kwantiteit is door het aantal in de cart + de nieuwe kwantieit te vergelijken met de
        // kwantiteit van de Product class

        cartItemRepository.save(cartItem);

        // update de huidige kwantiteit van je winkelmandje
        return modelMapper.map(cart, CartDTO.class);
    }

    private Cart createCart() {
        // Zoek de cart op van de ingelogde user zodat deze geupdate kan worden
        Cart userCart = cartRepository.findCartByUserEmail(authUtils.getLoggedInUser().getEmail());
        if (userCart != null) {
            return userCart;
        }

        // Creeer een nieuwe cart voor de ingelogde user als deze niet bestaat
        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtils.getLoggedInUser());

        return cartRepository.save(cart);
    }
}
