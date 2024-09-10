package com.ecommerce.project.service.order;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.repositories.*;
import com.ecommerce.project.service.cart.CartServiceImpl;
import com.ecommerce.project.util.AuthUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private ICartRepository cartRepository;
    @Autowired
    private IAddressRepository addressRepository;
    @Autowired
    private IPaymentRepository paymentRepository;
    @Autowired
    private IOrderRepository orderRepository;
    @Autowired
    private IOrderItemRepository orderItemRepository;
    @Autowired
    private IProductRepository productRepository;
    @Autowired
    private CartServiceImpl cartService;
    @Autowired
    private AuthUtils authUtils;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(order -> modelMapper.map(order, OrderDTO.class)).toList();
    }

    @Transactional
    @Override
    public OrderDTO placeOrder(
            Long addressId,
            String paymentMethod,
            String pgName,
            String pgPaymentId,
            String pgStatus,
            String pgResponseMessage
    ) {
        User user = authUtils.getLoggedInUser();

        // Vind de cart van de ingelogde user. Een cart moet namelijk omgezet worden naar een order
        Cart cart = cartRepository.findCartByUserEmail(user.getEmail());
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", user.getEmail());
        }

        // Kijk of het adres bestaat wat meegegeven wordt
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        // Maak een order object aan
        // TODO: Gebruik maken van een/de constructor?
        Order order = new Order();
        order.setEmail(user.getEmail());
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order accepted");
        order.setAddress(address);

        // Maak een payment object aan en sla ook op in de order want een payment is onderdeel van de order
        Payment payment = new Payment(paymentMethod, pgName, pgPaymentId, pgStatus, pgResponseMessage);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        order = orderRepository.save(order);

        // Haal alle cart items uit de cart op
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        // TODO: verschil in foreach onderzoeken
        // Converteer elk cartItem in een orderItem
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }

        // Sla alle orderItems op
        orderItems = orderItemRepository.saveAll(orderItems);

        // Kwantiteit van elk product aanpassen doordat de order uitgevoerd wordt en de cart legen
        cart.getCartItems().forEach(item -> {
            // TODO: Een check inbouwen of de quantity nog wel beschikbaar is
            int quantity = item.getQuantity();
            Product product = item.getProduct();

            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            // Verwijder het product uit de cart zodat de cart op een gegeven moment leeg komt te staan
            cartService.deleteProductFromCart(item.getProduct().getProductId());
        });

        // Converteer de order naar een DTO en sla elk orderItem op in de dto
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderItems.forEach(item -> {
            assert orderDTO != null;
            // TODO: getorderitems geeft null terug
            orderDTO.getOrderItems().add(modelMapper.map(item, OrderItem.class));
        });

        orderDTO.setAddressId(addressId);
        return orderDTO;
    }
}
