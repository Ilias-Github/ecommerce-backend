package com.ecommerce.project.controller;

import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        List<CartDTO> cartDTO = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTO, HttpStatus.FOUND);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getUserCart() {
        CartDTO cartDTO = cartService.getUserCart();
        return new ResponseEntity<>(cartDTO, HttpStatus.FOUND);
    }

    @PutMapping("/cart/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId, @PathVariable int quantity) {
        CartDTO cartDTO = cartService.updateCartQuantity(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable int quantity) {
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/carts/products/{productId}")
    public ResponseEntity<CartDTO> deleteProductFromCart(@PathVariable Long productId) {
        CartDTO cartDTO = cartService.deleteProductFromCart(productId);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }
}
