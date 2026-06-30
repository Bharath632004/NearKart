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

    boolean existsByPhone(String phone);
    boolean existsByAadhaarNumber(String aadhaarNumber);
    boolean existsByVehicleNumber(String vehicleNumber);

    Page<DeliveryPartner> findByStatus(PartnerStatus status, Pageable pageable);

    /**
     * Find ONLINE partners within a given radius (Haversine formula).
     * Radius is in kilometres.
     */
    @Query(value = """
        SELECT * FROM delivery_partners p
        WHERE p.status = 'ONLINE'
          AND p.current_latitude IS NOT NULL
          AND p.current_longitude IS NOT NULL
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
        LIMIT :limit
        """, nativeQuery = true)
    List<DeliveryPartner> findNearbyOnlinePartners(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("limit") int limit);
}
