package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.DeliveryAssignment;
import in.nearkart.delivery.entity.DeliveryPartner;
import in.nearkart.delivery.entity.AssignmentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {

    Optional<DeliveryAssignment> findByOrderId(UUID orderId);
    List<DeliveryAssignment> findByPartner(DeliveryPartner partner);
    List<DeliveryAssignment> findByPartnerAndStatus(DeliveryPartner partner, AssignmentStatus status);
    boolean existsByOrderId(UUID orderId);

    /** Called at DeliveryAssignmentServiceImpl line 206 */
    List<DeliveryAssignment> findByPartnerOrderByCreatedAtDesc(DeliveryPartner partner, Pageable pageable);
}
