package com.ecommerce.project.service.cart;

import com.ecommerce.project.payload.CartDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ICartService {
    List<CartDTO> getAllCarts();

    CartDTO getUserCart();

    CartDTO addProductToCart(Long productId, int quantity);

    @Transactional
    CartDTO updateCartQuantity(Long productId, int quantity);

    @Transactional
    void updateProductInCarts(Long cartId, Long productId);

    @Transactional
    CartDTO deleteProductFromCart(Long productId);
}
