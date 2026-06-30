package com.nearkart.orderservice.repository;

import com.nearkart.orderservice.model.Order;
import com.nearkart.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByShopId(Long shopId);
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);
    List<Order> findByShopIdAndStatus(Long shopId, OrderStatus status);
}
