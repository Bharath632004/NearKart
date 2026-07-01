package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.DeliveryPartner;
import in.nearkart.delivery.entity.PartnerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, UUID> {

    Optional<DeliveryPartner> findByPhone(String phone);

    Optional<DeliveryPartner> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    // Used by DeliveryPartnerServiceImpl.register()
    boolean existsByAadhaarNumber(String aadhaarNumber);

    boolean existsByVehicleNumber(String vehicleNumber);

    // Used by DeliveryPartnerServiceImpl.listByStatus()
    Page<DeliveryPartner> findByStatus(PartnerStatus status, Pageable pageable);

    @Query(value = """
        SELECT p.* FROM delivery_partners p
        WHERE p.status = 'ONLINE'
          AND p.is_available = true
          AND (
            6371 * acos(
              cos(radians(:lat)) * cos(radians(p.current_latitude))
              * cos(radians(p.current_longitude) - radians(:lng))
              + sin(radians(:lat)) * sin(radians(p.current_latitude))
            )
          ) <= :radiusKm
        ORDER BY (
            6371 * acos(
              cos(radians(:lat)) * cos(radians(p.current_latitude))
              * cos(radians(p.current_longitude) - radians(:lng))
              + sin(radians(:lat)) * sin(radians(p.current_latitude))
            )
        ) ASC
        LIMIT :maxResults
        """, nativeQuery = true)
    List<DeliveryPartner> findNearbyOnlinePartners(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("maxResults") int maxResults
    );
}
