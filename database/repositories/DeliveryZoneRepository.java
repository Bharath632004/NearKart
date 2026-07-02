package com.nearkart.repository;

import com.nearkart.entity.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, UUID> {

    List<DeliveryZone> findByShopIdAndIsActiveTrue(UUID shopId);

    Optional<DeliveryZone> findByShopIdAndPincode(UUID shopId, String pincode);

    @Query("""
            SELECT dz FROM DeliveryZone dz
            WHERE dz.pincode = :pincode
              AND dz.isActive = true
            ORDER BY dz.deliveryCharge
            """)
    List<DeliveryZone> findActiveByPincode(@Param("pincode") String pincode);
}
