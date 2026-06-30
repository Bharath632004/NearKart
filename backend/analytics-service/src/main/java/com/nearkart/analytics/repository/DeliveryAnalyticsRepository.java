package com.nearkart.analytics.repository;

import com.nearkart.analytics.model.DeliveryAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAnalyticsRepository extends JpaRepository<DeliveryAnalytics, Long> {

    Optional<DeliveryAnalytics> findByDate(LocalDate date);

    List<DeliveryAnalytics> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}
