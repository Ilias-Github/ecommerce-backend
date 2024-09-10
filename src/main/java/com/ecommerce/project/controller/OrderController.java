package com.ecommerce.project.controller;

import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.service.order.IOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getAllProducts() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@Valid @PathVariable String paymentMethod,
                                                  @RequestBody OrderRequestDTO orderRequestDTO) {
        OrderDTO orderDTO = orderService.placeOrder(
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );
        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
    }
}
