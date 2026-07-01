package in.nearkart.order.repository;

import in.nearkart.order.entity.Order;
import in.nearkart.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByShopIdOrderByCreatedAtDesc(Long shopId);

    List<Order> findByStatus(OrderStatus status);

    // Used by PaymentEventConsumer to resolve payment events to orders
    Optional<Order> findTopByStatusOrderByCreatedAtDesc(OrderStatus status);
}
