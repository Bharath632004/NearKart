package com.nearkart.analytics.repository;

import com.nearkart.analytics.model.SalesAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesAnalyticsRepository extends JpaRepository<SalesAnalytics, Long> {

    Optional<SalesAnalytics> findByDateAndPeriod(LocalDate date, SalesAnalytics.Period period);

    List<SalesAnalytics> findByPeriodAndDateBetweenOrderByDateAsc(
            SalesAnalytics.Period period, LocalDate from, LocalDate to);

    @Query("SELECT s FROM SalesAnalytics s WHERE s.period = :period ORDER BY s.date DESC")
    List<SalesAnalytics> findLatestByPeriod(SalesAnalytics.Period period,
            org.springframework.data.domain.Pageable pageable);
}
