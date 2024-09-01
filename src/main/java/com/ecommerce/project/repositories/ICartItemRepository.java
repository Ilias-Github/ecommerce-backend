package com.ecommerce.project.repositories;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ICartItemRepository extends JpaRepository<CartItem, Long> {
    // Custom query geschreven omdat product id niet gevonden werd
    // TODO: kan dit zonder custom query?
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    CartItem findCartItemByCartIdAndProductId(Long cartId, Long productId);
}
