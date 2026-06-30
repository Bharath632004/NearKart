package com.nearkart.orderservice.service;

import com.nearkart.orderservice.dto.CreateOrderRequest;
import com.nearkart.orderservice.model.Order;
import com.nearkart.orderservice.model.OrderItem;
import com.nearkart.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(Long customerId, CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryNotes(request.getDeliveryNotes());
        order.setStatus(Order.OrderStatus.PLACED);

        List<OrderItem> items = request.getItems().stream().map(req -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(req.getProductId());
            item.setProductName(req.getProductName());
            item.setQuantity(req.getQuantity());
            item.setUnitPrice(BigDecimal.valueOf(req.getUnitPrice()));
            return item;
        }).collect(Collectors.toList());

        order.setItems(items);

        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId);
    }

    public List<Order> getOrdersByMerchant(Long merchantId) {
        return orderRepository.findByMerchantIdOrderByPlacedAtDesc(merchantId);
    }

    @Transactional
    public Order updateStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = getById(orderId);
        order.setStatus(newStatus);

        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED -> order.setCancelledAt(LocalDateTime.now());
            default -> {}
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order assignDeliveryAgent(Long orderId, Long agentId) {
        Order order = getById(orderId);
        order.setDeliveryAgentId(agentId);
        order.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getById(orderId);
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
}
