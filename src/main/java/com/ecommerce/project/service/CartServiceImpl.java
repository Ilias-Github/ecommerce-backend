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

    public CartDTO addProductToCart(Long productId, int quantity) {
        // Huidige cart ophalen van de ingelogde user
        Cart cart = createCart();

        // Haal het product op dat toegevoegd dient te worden aan de cart
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Cart moet aangevuld worden met een cartItem
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
