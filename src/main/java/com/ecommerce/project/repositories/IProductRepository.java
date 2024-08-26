package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
    Product findByProductName(String productName);

    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageable);

    List<Product> findByProductNameLikeIgnoreCase(String productName);
}
