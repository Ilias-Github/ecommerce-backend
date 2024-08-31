package com.ecommerce.project.payload;

import com.ecommerce.project.payload.product.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long cartId;
    private double totalPrice;
    private List<ProductDTO> products = new ArrayList<>();
}
