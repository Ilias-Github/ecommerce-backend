package com.ecommerce.project.payload;

import com.ecommerce.project.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String email;
    private List<OrderItem> orderItems = new ArrayList<>();
    private LocalDate localDate;
    private PaymentDTO payment;
    private double totalAmount;
    private String orderStatus;
    private Long addressId;
}
