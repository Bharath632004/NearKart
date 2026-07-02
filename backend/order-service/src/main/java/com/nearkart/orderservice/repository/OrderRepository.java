package com.nearkart.orderservice.repository;

import com.nearkart.orderservice.model.Order;
import com.nearkart.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByShopId(Long shopId);

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Page<Order> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    List<Order> findByShopIdAndStatus(Long shopId, OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    long countByCustomerId(Long customerId);

    long countByShopId(Long shopId);

    long countByShopIdAndStatus(Long shopId, OrderStatus status);

    long countByCustomerIdAndStatus(Long customerId, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.createdAt < :cutoff")
    List<Order> findStalePendingOrders(@Param("cutoff") LocalDateTime cutoff);
}
