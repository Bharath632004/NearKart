package com.nearkart.orderservice.service;

import com.nearkart.orderservice.dto.*;
import com.nearkart.orderservice.exception.OrderNotFoundException;
import com.nearkart.orderservice.model.Order;
import com.nearkart.orderservice.model.OrderItem;
import com.nearkart.orderservice.model.OrderStatus;
import com.nearkart.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .shopId(request.getShopId())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryPhone(request.getDeliveryPhone())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<OrderItem> items = request.getItems().stream().map(i -> {
            BigDecimal total = i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            return OrderItem.builder()
                    .productId(i.getProductId())
                    .productName(i.getProductName())
                    .quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice())
                    .totalPrice(total)
                    .order(order)
                    .build();
        }).collect(Collectors.toList());

        order.setItems(items);
        BigDecimal total = items.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        return toResponse(orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id)));
    }

    @Override
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByShop(Long shopId) {
        return orderRepository.findByShopId(shopId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    @Override
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> itemResponses = o.getItems().stream().map(i ->
                OrderItemResponse.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice())
                        .build()
        ).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId())
                .customerId(o.getCustomerId())
                .shopId(o.getShopId())
                .status(o.getStatus())
                .items(itemResponses)
                .totalAmount(o.getTotalAmount())
                .deliveryAddress(o.getDeliveryAddress())
                .deliveryPhone(o.getDeliveryPhone())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
