package com.nearkart.analytics.service;

import com.nearkart.analytics.model.MerchantAnalytics;
import com.nearkart.analytics.repository.MerchantAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantAnalyticsService {

    private final MerchantAnalyticsRepository repository;

    @Cacheable(value = "merchant-analytics", key = "#merchantId + '_' + #from + '_' + #to")
    public List<MerchantAnalytics> getMerchantReport(String merchantId, LocalDate from, LocalDate to) {
        return repository.findByMerchantIdAndDateBetweenOrderByDateAsc(merchantId, from, to);
    }

    @Cacheable(value = "top-merchants", key = "#date")
    public List<MerchantAnalytics> getTopMerchantsByRevenue(LocalDate date) {
        return repository.findByDateOrderByTotalRevenueDesc(date);
    }
}
