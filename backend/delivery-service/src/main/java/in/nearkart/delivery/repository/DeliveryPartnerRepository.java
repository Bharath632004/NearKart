package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.DeliveryPartner;
import in.nearkart.delivery.entity.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByPhone(String phone);
    Optional<DeliveryPartner> findByEmail(String email);
    List<DeliveryPartner> findByStatus(PartnerStatus status);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
}
