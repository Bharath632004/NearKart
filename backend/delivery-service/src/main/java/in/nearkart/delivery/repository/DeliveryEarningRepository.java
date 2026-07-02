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
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryEarningRepository extends JpaRepository<DeliveryEarning, UUID> {

    // Paginated + sorted by createdAt descending
    Page<DeliveryEarning> findByPartnerOrderByCreatedAtDesc(DeliveryPartner partner, Pageable pageable);

    // Convenience method if you also need a non-page list (optional)
    List<DeliveryEarning> findByPartnerOrderByCreatedAtDesc(DeliveryPartner partner);

    @Query("SELECT SUM(e.amount) FROM DeliveryEarning e WHERE e.partner = :partner AND e.settled = false")
    BigDecimal sumUnsettledByPartner(@Param("partner") DeliveryPartner partner);

    List<DeliveryEarning> findByPartnerAndSettled(DeliveryPartner partner, boolean settled);
}
