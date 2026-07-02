package com.nearkart.shopservice.repository;

import com.nearkart.shopservice.model.Shop;
import com.nearkart.shopservice.model.ShopCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByMerchantId(Long merchantId);

    List<Shop> findByActiveTrue();

    List<Shop> findByCategoryAndActiveTrue(ShopCategory category);

    List<Shop> findByCityAndActiveTrue(String city);

    List<Shop> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    Page<Shop> findByMerchantId(Long merchantId, Pageable pageable);

    Page<Shop> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    Optional<Shop> findByMerchantIdAndNameIgnoreCaseAndCity(Long merchantId, String name, String city);

    long countByMerchantId(Long merchantId);

    long countByActiveTrueAndCity(String city);

    @Query(value = "SELECT * FROM shops s WHERE s.active = true AND " +
           "(6371 * acos(LEAST(1.0, cos(radians(:lat)) * cos(radians(s.latitude)) * " +
           "cos(radians(s.longitude) - radians(:lng)) + " +
           "sin(radians(:lat)) * sin(radians(s.latitude))))) < :radiusKm",
           nativeQuery = true)
    List<Shop> findNearbyShops(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusKm") double radiusKm);

    @Query(value = "SELECT * FROM shops s WHERE s.active = true AND s.category = :category AND " +
           "(6371 * acos(LEAST(1.0, cos(radians(:lat)) * cos(radians(s.latitude)) * " +
           "cos(radians(s.longitude) - radians(:lng)) + " +
           "sin(radians(:lat)) * sin(radians(s.latitude))))) < :radiusKm",
           nativeQuery = true)
    List<Shop> findNearbyShopsByCategory(@Param("lat") double lat, @Param("lng") double lng,
                                          @Param("radiusKm") double radiusKm, @Param("category") String category);
}
