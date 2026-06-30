package com.nearkart.shopservice.repository;

import com.nearkart.shopservice.model.Shop;
import com.nearkart.shopservice.model.ShopCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByMerchantId(Long merchantId);
    List<Shop> findByActiveTrue();
    List<Shop> findByCategory(ShopCategory category);
    List<Shop> findByCityAndActiveTrue(String city);
    List<Shop> findByNameContainingIgnoreCase(String name);

    @Query("SELECT s FROM Shop s WHERE s.active = true AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude)) * " +
           "cos(radians(s.longitude) - radians(:lng)) + " +
           "sin(radians(:lat)) * sin(radians(s.latitude)))) < :radiusKm")
    List<Shop> findNearbyShops(double lat, double lng, double radiusKm);
}
