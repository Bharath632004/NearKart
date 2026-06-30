package com.nearkart.merchant.repository;

import com.nearkart.merchant.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {

    List<Shop> findByMerchantId(UUID merchantId);

    Optional<Shop> findByIdAndMerchantId(UUID id, UUID merchantId);

    @Query(value = "SELECT s.* FROM shops s WHERE s.is_active = true " +
            "AND ST_DWithin(s.location::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
            "ORDER BY ST_Distance(s.location::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography)",
            nativeQuery = true)
    List<Shop> findNearbyShops(@Param("latitude") double latitude,
                               @Param("longitude") double longitude,
                               @Param("radiusMeters") double radiusMeters);

    @Query(value = "SELECT s.* FROM shops s WHERE s.is_active = true AND s.category = :category " +
            "AND ST_DWithin(s.location::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
            "ORDER BY ST_Distance(s.location::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography)",
            nativeQuery = true)
    List<Shop> findNearbyShopsByCategory(@Param("latitude") double latitude,
                                          @Param("longitude") double longitude,
                                          @Param("radiusMeters") double radiusMeters,
                                          @Param("category") String category);

    boolean existsByMerchantIdAndShopName(UUID merchantId, String shopName);
}
