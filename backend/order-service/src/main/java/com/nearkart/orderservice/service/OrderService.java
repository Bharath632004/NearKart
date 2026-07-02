package com.nearkart.orderservice.service;

import com.nearkart.orderservice.dto.OrderRequest;
import com.nearkart.orderservice.dto.OrderResponse;
import com.nearkart.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrdersByCustomer(Long customerId);

    Page<OrderResponse> getOrdersByCustomerPaged(Long customerId, Pageable pageable);

    List<OrderResponse> getOrdersByCustomerAndStatus(Long customerId, OrderStatus status);

    List<OrderResponse> getOrdersByShop(Long shopId);

    Page<OrderResponse> getOrdersByShopPaged(Long shopId, Pageable pageable);

    List<OrderResponse> getOrdersByShopAndStatus(Long shopId, OrderStatus status);

    OrderResponse updateOrderStatus(Long id, OrderStatus newStatus);

    OrderResponse cancelOrder(Long id, String reason);

    OrderResponse returnOrder(Long id, String reason);

    OrderResponse initiateRefund(Long id);

    OrderResponse completeRefund(Long id);

    long countOrdersByCustomer(Long customerId);

    long countOrdersByShop(Long shopId);
}
