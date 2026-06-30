package com.nearkart.analytics.service;

import com.nearkart.analytics.model.CustomerAnalytics;
import com.nearkart.analytics.repository.CustomerAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerAnalyticsService {

    private final CustomerAnalyticsRepository repository;

    @Cacheable(value = "customer-analytics", key = "#date")
    public CustomerAnalytics getByDate(LocalDate date) {
        return repository.findByDate(date).orElse(null);
    }

    @Cacheable(value = "customer-analytics-range", key = "#from + '_' + #to")
    public List<CustomerAnalytics> getByRange(LocalDate from, LocalDate to) {
        return repository.findByDateBetweenOrderByDateAsc(from, to);
    }
}
