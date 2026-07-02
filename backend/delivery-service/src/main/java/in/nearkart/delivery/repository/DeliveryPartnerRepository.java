package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.DeliveryPartner;
import in.nearkart.delivery.entity.PartnerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    /** Used by DeliveryPartnerServiceImpl (paginated) */
    Page<DeliveryPartner> findByStatus(PartnerStatus status, Pageable pageable);

    /** Used by simple list queries */
    List<DeliveryPartner> findByStatus(PartnerStatus status);

    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByAadhaarNumber(String aadhaarNumber);
    boolean existsByVehicleNumber(String vehicleNumber);

    /**
     * Haversine query – returns ONLINE partners within radiusKm kilometres,
     * ordered by distance ascending.
     */
    @Query("""
        SELECT p FROM DeliveryPartner p
        WHERE p.status = 'ONLINE'
          AND (6371 * acos(
                 cos(radians(:lat)) * cos(radians(p.currentLatitude))
                 * cos(radians(p.currentLongitude) - radians(:lng))
                 + sin(radians(:lat)) * sin(radians(p.currentLatitude))
               )) < :radiusKm
        ORDER BY (6371 * acos(
                 cos(radians(:lat)) * cos(radians(p.currentLatitude))
                 * cos(radians(p.currentLongitude) - radians(:lng))
                 + sin(radians(:lat)) * sin(radians(p.currentLatitude))
               ))
        """)
    List<DeliveryPartner> findNearbyOnlinePartners(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusKm") double radiusKm,
            Pageable pageable);

    /** Convenience overload matching the (Double, Double, double, int) call-site */
    default List<DeliveryPartner> findNearbyOnlinePartners(
            Double lat, Double lng, double radiusKm, int limit) {
        return findNearbyOnlinePartners(lat, lng, radiusKm, PageRequest.of(0, limit));
    }
}
