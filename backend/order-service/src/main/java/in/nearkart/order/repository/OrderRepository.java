package in.nearkart.order.repository;

import in.nearkart.order.entity.Order;
import in.nearkart.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByShopIdOrderByCreatedAtDesc(Long shopId);

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Order> findLatestByStatus(@Param("status") OrderStatus status);
}
