package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.DeliveryAssignment;
import in.nearkart.delivery.entity.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    Optional<DeliveryAssignment> findByOrderId(Long orderId);
    List<DeliveryAssignment> findByPartnerId(Long partnerId);
    List<DeliveryAssignment> findByPartnerIdAndStatus(Long partnerId, AssignmentStatus status);
    boolean existsByOrderId(Long orderId);
}
