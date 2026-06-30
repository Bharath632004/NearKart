package com.nearkart.analytics.service;

import com.nearkart.analytics.model.CustomerAnalytics;
import com.nearkart.analytics.model.DeliveryAnalytics;
import com.nearkart.analytics.model.SalesAnalytics;
import com.nearkart.analytics.repository.CustomerAnalyticsRepository;
import com.nearkart.analytics.repository.DeliveryAnalyticsRepository;
import com.nearkart.analytics.repository.SalesAnalyticsRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SalesAnalyticsRepository salesRepo;
    private final DeliveryAnalyticsRepository deliveryRepo;
    private final CustomerAnalyticsRepository customerRepo;

    @Cacheable(value = "dashboard-summary", key = "#date")
    public Map<String, Object> getDashboardSummary(LocalDate date) {
        Map<String, Object> dashboard = new HashMap<>();

        salesRepo.findByDateAndPeriod(date, SalesAnalytics.Period.DAILY)
                .ifPresent(s -> {
                    dashboard.put("totalRevenue", s.getTotalRevenue());
                    dashboard.put("totalOrders", s.getTotalOrders());
                    dashboard.put("averageOrderValue", s.getAverageOrderValue());
                    dashboard.put("totalCommission", s.getTotalCommission());
                });

        deliveryRepo.findByDate(date)
                .ifPresent(d -> {
                    dashboard.put("totalDeliveries", d.getTotalDeliveries());
                    dashboard.put("onTimeDeliveries", d.getOnTimeDeliveries());
                    dashboard.put("slaBreachRate", d.getSlaBreachRate());
                    dashboard.put("avgDeliveryTime", d.getAverageDeliveryTimeMinutes());
                });

        customerRepo.findByDate(date)
                .ifPresent(c -> {
                    dashboard.put("newCustomers", c.getNewCustomers());
                    dashboard.put("activeCustomers", c.getActiveCustomers());
                    dashboard.put("retentionRate", c.getRetentionRate());
                });

        dashboard.put("date", date.toString());
        return dashboard;
    }
}
