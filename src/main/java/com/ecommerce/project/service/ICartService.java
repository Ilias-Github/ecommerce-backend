package com.ecommerce.project.service;

import com.ecommerce.project.payload.CartDTO;

public interface ICartService {
    CartDTO addProductToCart(Long productId, int quantity);
}
