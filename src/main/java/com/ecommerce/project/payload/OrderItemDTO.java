package com.ecommerce.project.payload;

import com.ecommerce.project.payload.product.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private ProductDTO productDTO;
    private int quantity;
    private double discount;
    private double orderedProductPrice;
}
