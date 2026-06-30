package com.nearkart.analytics.repository;

import com.nearkart.analytics.model.MerchantAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantAnalyticsRepository extends JpaRepository<MerchantAnalytics, Long> {

    Optional<MerchantAnalytics> findByMerchantIdAndDate(String merchantId, LocalDate date);

    List<MerchantAnalytics> findByMerchantIdAndDateBetweenOrderByDateAsc(
            String merchantId, LocalDate from, LocalDate to);

    List<MerchantAnalytics> findByDateOrderByTotalRevenueDesc(LocalDate date);
}
