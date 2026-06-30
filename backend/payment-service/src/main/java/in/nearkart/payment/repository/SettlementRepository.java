package in.nearkart.payment.repository;

import in.nearkart.payment.entity.Settlement;
import in.nearkart.payment.entity.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    Page<Settlement> findByShopIdOrderByCreatedAtDesc(UUID shopId, Pageable pageable);
    List<Settlement> findByStatus(SettlementStatus status);

    @Query("SELECT s FROM Settlement s WHERE s.shopId = :shopId " +
           "AND s.periodStart >= :from AND s.periodEnd <= :to")
    List<Settlement> findByShopAndPeriod(UUID shopId, LocalDate from, LocalDate to);
}
