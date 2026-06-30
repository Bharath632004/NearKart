package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.DeliveryEarning;
import in.nearkart.delivery.entity.DeliveryPartner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface DeliveryEarningRepository extends JpaRepository<DeliveryEarning, UUID> {

    Page<DeliveryEarning> findByPartnerOrderByCreatedAtDesc(DeliveryPartner partner, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM DeliveryEarning e WHERE e.partner = :partner AND e.settled = false")
    BigDecimal sumUnsettledEarnings(@Param("partner") DeliveryPartner partner);
}
