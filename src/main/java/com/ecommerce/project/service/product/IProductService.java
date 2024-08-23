package com.ecommerce.project.service.product;

import com.ecommerce.project.payload.product.ProductDTO;
import com.ecommerce.project.payload.product.ProductResponse;

public interface IProductService {
    ProductResponse getAllProducts();

    ProductDTO createProduct(ProductDTO productDTO, Long categoryId);
}
