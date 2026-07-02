package com.nearkart.database.repositories;

import com.nearkart.domain.shop.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Shop entity.
 * Includes geospatial queries using PostGIS ST_DWithin.
 */
@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {

    // Shops owned by a merchant
    List<Shop> findByMerchantIdAndDeletedAtIsNull(UUID merchantId);

    // Shops by category that are active and verified
    Page<Shop> findByCategoryIdAndIsActiveTrueAndIsVerifiedTrueAndDeletedAtIsNull(
            Integer categoryId, Pageable pageable);

    // Nearby shops within radius (km) using PostGIS
    @Query(value = "SELECT * FROM shops "
                 + "WHERE ST_DWithin("
                 + "  ST_MakePoint(longitude, latitude)::geography,"
                 + "  ST_MakePoint(:lng, :lat)::geography,"
                 + "  :radiusMeters"
                 + ") AND is_active = TRUE AND is_verified = TRUE AND deleted_at IS NULL "
                 + "ORDER BY ST_Distance("
                 + "  ST_MakePoint(longitude, latitude)::geography,"
                 + "  ST_MakePoint(:lng, :lat)::geography"
                 + ") ASC LIMIT :limit",
           nativeQuery = true)
    List<Shop> findNearbyShops(@Param("lat") double lat,
                               @Param("lng") double lng,
                               @Param("radiusMeters") double radiusMeters,
                               @Param("limit") int limit);

    // Search shops by name
    @Query(value = "SELECT * FROM shops WHERE name ILIKE '%' || :name || '%' "
                 + "AND is_active = TRUE AND deleted_at IS NULL LIMIT 20",
           nativeQuery = true)
    List<Shop> searchByName(@Param("name") String name);
}
