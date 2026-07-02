package com.nearkart.repository.analytics;

import com.nearkart.entity.analytics.AnalyticsDailyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalyticsDailyMetricsRepository
        extends JpaRepository<AnalyticsDailyMetrics, UUID> {

    Optional<AnalyticsDailyMetrics> findByMetricDate(LocalDate date);

    @Query("""
            SELECT m FROM AnalyticsDailyMetrics m
            WHERE m.metricDate BETWEEN :from AND :to
            ORDER BY m.metricDate
            """)
    List<AnalyticsDailyMetrics> findByDateRange(
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to);
}
