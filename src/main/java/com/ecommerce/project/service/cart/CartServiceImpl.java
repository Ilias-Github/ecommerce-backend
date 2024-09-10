package com.ecommerce.project.service.cart;

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
import jakarta.transaction.Transactional;
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

        List<ProductDTO> productDTOS = cart.getCartItems().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        // TODO: fix description van product
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

    // Om de integriteit van de data te behouden indien de functie niet volledig wordt uitgevoerd, wil je dat alle
    // veranderingen teruggedraaid worden. Daarvoor is de @Transactional annotation
    @Transactional
    @Override
    public CartDTO updateCartQuantity(Long productId, int quantity) {
        // Haal de cart op
        Cart cart = createCart();

        // Vind het cartItem van de huidige user dat overeenkomt met het product
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getId(), productId);

        // Haal het product op waarvan de quantity gecontroleerd dient te worden
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (cartItem == null) {
            cartItem = new CartItem();

            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
            cartItem.setCart(cart);
        }

        if (quantity > product.getQuantity()) {
            throw new APIException("Quantity exceeds available product quantity");
        }

        // TODO: Verwijder product uit cart wanneer quantity op 0 wordt gezet
        if (quantity < 0) {
            throw new APIException("Quantity can't be lower than 0");
        }

        // Update de quantity van de cartitem dat verbonden is met de cart van de user
        cartItem.setQuantity(quantity);
        // TODO: automatisch prijs uitrekenen van de cart
        // TODO: Voeg de producten toe aan de cartdto zodat de producten te zien zijn in de response
        cartItemRepository.save(cartItem);

        // TODO: method voor schrijven? (volgens mij gebeurt dit vaker in deze class)
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        List<ProductDTO> productDTOStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        }).toList();

        cartDTO.setProducts(productDTOStream);

        return cartDTO;
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        // Haal de cart op
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        // Haal het geupdatet product op
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Haal het cartItem op dat geupdatet dient te worden
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId);

        // TODO: Kan hier niet een orElseThrow van gemaakt worden?
        if (cartItem == null) {
            throw new APIException("Product" + product.getProductName() + " not found");
        }

        // TODO: in de tutorial zat dit onder de double, klopt dit wel? Moet het niet zoals het er nu staat?
        cartItem.setProductPrice(product.getSpecialPrice());

        // Zet de nieuwe total cart price aan de hand van het geupdatet product
        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        // Zet de nieuwe cart price
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.save(cartItem);
    }


    // TODO: Geen een string terug met dat het gelukt is
    @Transactional
    @Override
    public CartDTO deleteProductFromCart(Long productId) {
        Cart cart = cartRepository.findCartByUserEmail(authUtils.getLoggedInUser().getEmail());

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getId(), productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cartItemRepository.delete(cartItem);
        // TODO: Zet de nieuwe total price
        return modelMapper.map(cart, CartDTO.class);
    }

    // TODO: controleren of dit nodig is
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
