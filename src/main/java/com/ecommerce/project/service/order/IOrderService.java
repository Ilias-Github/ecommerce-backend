package com.ecommerce.project.service.order;

import com.ecommerce.project.payload.OrderDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public interface IOrderService {
    @Transactional
    OrderDTO placeOrder(Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);
}
