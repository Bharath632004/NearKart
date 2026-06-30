package com.nearkart.analytics.service;

import com.nearkart.analytics.model.SalesAnalytics;
import com.nearkart.analytics.repository.SalesAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesAnalyticsService {

    private final SalesAnalyticsRepository repository;

    @Cacheable(value = "sales-daily", key = "#date")
    public SalesAnalytics getDailySales(LocalDate date) {
        return repository.findByDateAndPeriod(date, SalesAnalytics.Period.DAILY)
                .orElse(null);
    }

    @Cacheable(value = "sales-range", key = "#period + '_' + #from + '_' + #to")
    public List<SalesAnalytics> getSalesByRange(SalesAnalytics.Period period, LocalDate from, LocalDate to) {
        return repository.findByPeriodAndDateBetweenOrderByDateAsc(period, from, to);
    }

    @Cacheable(value = "sales-latest", key = "#period + '_' + #count")
    public List<SalesAnalytics> getLatestSales(SalesAnalytics.Period period, int count) {
        return repository.findLatestByPeriod(period, PageRequest.of(0, count));
    }
}
