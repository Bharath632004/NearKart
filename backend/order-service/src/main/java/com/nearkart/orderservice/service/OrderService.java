package com.nearkart.orderservice.service;

import com.nearkart.orderservice.dto.OrderRequest;
import com.nearkart.orderservice.dto.OrderResponse;
import com.nearkart.orderservice.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getOrdersByCustomer(Long customerId);
    List<OrderResponse> getOrdersByShop(Long shopId);
    OrderResponse updateOrderStatus(Long id, OrderStatus status);
    void cancelOrder(Long id);
}
