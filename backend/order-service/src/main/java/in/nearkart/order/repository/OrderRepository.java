package in.nearkart.order.repository;

import in.nearkart.order.entity.Order;
import in.nearkart.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Page<Order> findByShopIdOrderByCreatedAtDesc(UUID shopId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(
            UUID customerId, OrderStatus status, Pageable pageable);

    long countByShopIdAndStatus(UUID shopId, OrderStatus status);
}
