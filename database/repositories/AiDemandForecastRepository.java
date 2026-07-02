package com.nearkart.repository.ai;

import com.nearkart.entity.ai.AiDemandForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiDemandForecastRepository extends JpaRepository<AiDemandForecast, UUID> {

    Optional<AiDemandForecast> findByProductIdAndShopIdAndForecastDateAndModelVersion(
            UUID productId, UUID shopId, LocalDate forecastDate, String modelVersion);

    @Query("""
            SELECT f FROM AiDemandForecast f
            WHERE f.shopId = :shopId
              AND f.forecastDate BETWEEN :from AND :to
            ORDER BY f.forecastDate
            """)
    List<AiDemandForecast> findByShopIdAndDateRange(
            @Param("shopId") UUID shopId,
            @Param("from")   LocalDate from,
            @Param("to")     LocalDate to);
}
