package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.DeliveryEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeliveryEarningRepository extends JpaRepository<DeliveryEarning, Long> {
    List<DeliveryEarning> findByPartnerId(Long partnerId);

    @Query("SELECT SUM(e.amount) FROM DeliveryEarning e WHERE e.partnerId = :partnerId")
    BigDecimal sumAmountByPartnerId(@Param("partnerId") Long partnerId);

    @Query("SELECT SUM(e.amount) FROM DeliveryEarning e WHERE e.partnerId = :partnerId AND e.earnedDate = :date")
    BigDecimal sumAmountByPartnerIdAndDate(@Param("partnerId") Long partnerId, @Param("date") LocalDate date);
}
