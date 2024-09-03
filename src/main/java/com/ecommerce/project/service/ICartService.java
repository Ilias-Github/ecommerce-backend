package com.ecommerce.project.service;

import com.ecommerce.project.payload.CartDTO;

import java.util.List;

public interface ICartService {
    List<CartDTO> getAllCarts();

    CartDTO addProductToCart(Long productId, int quantity);
}
