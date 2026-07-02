package com.nearkart.repository.analytics;

import com.nearkart.entity.analytics.AnalyticsShopDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalyticsShopDailyRepository extends JpaRepository<AnalyticsShopDaily, UUID> {

    @Query("""
            SELECT s FROM AnalyticsShopDaily s
            WHERE s.shopId = :shopId
              AND s.metricDate BETWEEN :from AND :to
            ORDER BY s.metricDate DESC
            """)
    List<AnalyticsShopDaily> findByShopAndDateRange(
            @Param("shopId") UUID shopId,
            @Param("from")   LocalDate from,
            @Param("to")     LocalDate to);

    @Query("""
            SELECT s FROM AnalyticsShopDaily s
            WHERE s.shopId = :shopId
            ORDER BY s.metricDate DESC
            """)
    List<AnalyticsShopDaily> findLatestByShop(
            @Param("shopId") UUID shopId,
            org.springframework.data.domain.Pageable pageable);
}
