package com.nearkart.analytics.service;

import com.nearkart.analytics.model.DeliveryAnalytics;
import com.nearkart.analytics.repository.DeliveryAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAnalyticsService {

    private final DeliveryAnalyticsRepository repository;

    @Cacheable(value = "delivery-today")
    public DeliveryAnalytics getToday() {
        return repository.findByDate(LocalDate.now()).orElse(null);
    }

    @Cacheable(value = "delivery-range", key = "#from + '_' + #to")
    public List<DeliveryAnalytics> getSlaReport(LocalDate from, LocalDate to) {
        return repository.findByDateBetweenOrderByDateAsc(from, to);
    }
}
