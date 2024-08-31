package com.ecommerce.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    @NotBlank
    @Size(min = 3, message = "Product name must contain at least 3 characters")
    private String productName;
    @NotBlank
    @Size(min = 6, message = "Description must contain at least 6 characters")
    private String description;
    private String image;
    private int quantity;
    @NotNull
    private double price;
    private double discount;
    @NotNull
    private double specialPrice;
    @ManyToOne
    @JsonIgnore
    private Category category;

    // Meerdere producten kunnen verkocht worden door een user
    @ManyToOne
    private User user;

    // Eager omdat de producten opgehaald moeten worden wanneer de cart items opgehaald worden
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private List<CartItem> cartItem;
}
