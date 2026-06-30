package in.nearkart.payment.repository;

import in.nearkart.payment.entity.Settlement;
import in.nearkart.payment.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    List<Settlement> findBySellerId(UUID sellerId);
    List<Settlement> findByStatus(SettlementStatus status);
    Optional<Settlement> findByPaymentId(UUID paymentId);
}
