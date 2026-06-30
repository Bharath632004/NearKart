package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.DeliveryAssignment;
import in.nearkart.delivery.entity.DeliveryPartner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {

    Optional<DeliveryAssignment> findByOrderId(UUID orderId);

    Page<DeliveryAssignment> findByPartnerOrderByCreatedAtDesc(DeliveryPartner partner, Pageable pageable);
}
