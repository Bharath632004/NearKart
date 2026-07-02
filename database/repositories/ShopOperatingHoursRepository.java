package com.nearkart.repository;

import com.nearkart.entity.ShopOperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopOperatingHoursRepository extends JpaRepository<ShopOperatingHours, UUID> {

    List<ShopOperatingHours> findByShopIdOrderByDayOfWeek(UUID shopId);

    Optional<ShopOperatingHours> findByShopIdAndDayOfWeek(UUID shopId, int dayOfWeek);

    @Query("""
            SELECT h FROM ShopOperatingHours h
            WHERE h.shopId = :shopId
              AND h.dayOfWeek = :day
              AND h.isClosed = false
            """)
    Optional<ShopOperatingHours> findOpenHours(
            @Param("shopId") UUID shopId,
            @Param("day")    int day);
}
