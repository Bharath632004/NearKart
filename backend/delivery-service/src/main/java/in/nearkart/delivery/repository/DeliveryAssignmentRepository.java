package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.AssignmentStatus;
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

    Optional<DeliveryAssignment> findByOrderNumber(String orderNumber);

    Page<DeliveryAssignment> findByPartnerOrderByCreatedAtDesc(DeliveryPartner partner, Pageable pageable);

    Page<DeliveryAssignment> findByStatusOrderByCreatedAtDesc(AssignmentStatus status, Pageable pageable);

    Optional<DeliveryAssignment> findByPartnerAndStatusIn(
            DeliveryPartner partner, java.util.List<AssignmentStatus> statuses);
}
