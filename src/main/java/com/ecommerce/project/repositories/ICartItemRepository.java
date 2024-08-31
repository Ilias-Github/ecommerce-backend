package com.ecommerce.project.repositories;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICartItemRepository extends JpaRepository<CartItem, Long> {
}
