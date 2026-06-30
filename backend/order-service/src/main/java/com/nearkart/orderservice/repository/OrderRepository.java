package com.nearkart.orderservice.repository;

import com.nearkart.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId);
    List<Order> findByMerchantIdOrderByPlacedAtDesc(Long merchantId);
    List<Order> findByDeliveryAgentIdAndStatus(Long agentId, Order.OrderStatus status);
    List<Order> findByStatus(Order.OrderStatus status);
}
