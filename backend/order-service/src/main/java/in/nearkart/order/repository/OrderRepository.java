package in.nearkart.order.repository;

import in.nearkart.order.entity.Order;
import in.nearkart.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Page<Order> findByShopIdOrderByCreatedAtDesc(UUID shopId, Pageable pageable);

    Page<Order> findByShopIdAndStatusOrderByCreatedAtDesc(UUID shopId, OrderStatus status, Pageable pageable);

    List<Order> findByShopIdAndStatusIn(UUID shopId, List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.shopId = :shopId " +
           "AND o.createdAt BETWEEN :from AND :to " +
           "AND o.status = 'DELIVERED'")
    List<Order> findDeliveredOrdersByShopAndDateRange(
            UUID shopId, LocalDateTime from, LocalDateTime to);

    long countByShopIdAndStatus(UUID shopId, OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId " +
           "AND o.status NOT IN ('CANCELLED', 'REFUNDED')")
    long countActiveOrdersByCustomer(UUID customerId);
}
