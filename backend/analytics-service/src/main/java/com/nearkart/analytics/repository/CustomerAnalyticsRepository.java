package com.nearkart.analytics.repository;

import com.nearkart.analytics.model.CustomerAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAnalyticsRepository extends JpaRepository<CustomerAnalytics, Long> {

    Optional<CustomerAnalytics> findByDate(LocalDate date);

    List<CustomerAnalytics> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}
