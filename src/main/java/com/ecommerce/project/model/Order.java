package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // TODO: is dit veld nodig gezien een order al gekoppeld is aan een user
    @Email
    @Column
    private String email;
    private LocalDate orderDate;
    private double totalAmount;
    private String orderStatus;

    @OneToOne
    private Payment payment;

    @ManyToOne
    private Address address;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<OrderItem> orderItemList = new ArrayList<>();
}
