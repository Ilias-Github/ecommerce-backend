package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    private String productName;
    private String description;
    private String image;
    private int quantity;
    @NotNull
    private double price;
    private double discount;
    @NotNull
    private double specialPrice;
    @ManyToOne
    private Category category;

}
