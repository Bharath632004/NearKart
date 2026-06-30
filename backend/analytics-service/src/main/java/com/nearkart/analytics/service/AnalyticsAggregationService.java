package com.nearkart.analytics.service;

import com.nearkart.analytics.dto.OrderEvent;
import com.nearkart.analytics.model.*;
import com.nearkart.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsAggregationService {

    private final SalesAnalyticsRepository salesRepo;
    private final MerchantAnalyticsRepository merchantRepo;
    private final DeliveryAnalyticsRepository deliveryRepo;
    private final CustomerAnalyticsRepository customerRepo;

    @Transactional
    public void processOrderEvent(OrderEvent event) {
        if (!"DELIVERED".equals(event.getStatus()) && !"PLACED".equals(event.getStatus())) return;

        LocalDate today = event.getEventTime().toLocalDate();

        // --- Sales Analytics ---
        SalesAnalytics sales = salesRepo.findByDateAndPeriod(today, SalesAnalytics.Period.DAILY)
                .orElseGet(() -> SalesAnalytics.builder()
                        .date(today)
                        .period(SalesAnalytics.Period.DAILY)
                        .totalRevenue(java.math.BigDecimal.ZERO)
                        .totalOrders(0L)
                        .totalItems(0L)
                        .totalCommission(java.math.BigDecimal.ZERO)
                        .build());

        if ("PLACED".equals(event.getStatus())) {
            sales.setTotalOrders(sales.getTotalOrders() + 1);
            sales.setTotalItems(sales.getTotalItems() + event.getItemCount());
            sales.setTotalRevenue(sales.getTotalRevenue().add(event.getOrderAmount()));
            sales.setTotalCommission(sales.getTotalCommission().add(event.getCommissionAmount()));
            if (sales.getTotalOrders() > 0) {
                sales.setAverageOrderValue(sales.getTotalRevenue()
                        .divide(java.math.BigDecimal.valueOf(sales.getTotalOrders()),
                                2, java.math.RoundingMode.HALF_UP));
            }
        }
        salesRepo.save(sales);

        // --- Merchant Analytics ---
        MerchantAnalytics merchant = merchantRepo.findByMerchantIdAndDate(event.getMerchantId(), today)
                .orElseGet(() -> MerchantAnalytics.builder()
                        .merchantId(event.getMerchantId())
                        .date(today)
                        .totalOrders(0L)
                        .totalRevenue(java.math.BigDecimal.ZERO)
                        .totalCommissionPaid(java.math.BigDecimal.ZERO)
                        .cancelledOrders(0L)
                        .build());

        if ("PLACED".equals(event.getStatus())) {
            merchant.setTotalOrders(merchant.getTotalOrders() + 1);
            merchant.setTotalRevenue(merchant.getTotalRevenue().add(event.getOrderAmount()));
            merchant.setTotalCommissionPaid(merchant.getTotalCommissionPaid().add(event.getCommissionAmount()));
        }
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantRepo.save(merchant);

        // --- Customer Analytics ---
        if ("PLACED".equals(event.getStatus()) && event.getCustomerId() != null) {
            CustomerAnalytics customer = customerRepo.findByDate(today)
                    .orElseGet(() -> CustomerAnalytics.builder()
                            .date(today)
                            .newCustomers(0L)
                            .activeCustomers(0L)
                            .returningCustomers(0L)
                            .totalCartAbandoned(0L)
                            .averageSpendPerCustomer(java.math.BigDecimal.ZERO)
                            .retentionRate(0.0)
                            .build());
            customer.setActiveCustomers(customer.getActiveCustomers() + 1);
            customer.setUpdatedAt(LocalDateTime.now());
            customerRepo.save(customer);
        }
    }

    @Transactional
    public void processDeliveryEvent(OrderEvent event) {
        LocalDate today = event.getEventTime().toLocalDate();

        DeliveryAnalytics delivery = deliveryRepo.findByDate(today)
                .orElseGet(() -> DeliveryAnalytics.builder()
                        .date(today)
                        .totalDeliveries(0L)
                        .onTimeDeliveries(0L)
                        .lateDeliveries(0L)
                        .failedDeliveries(0L)
                        .averageDeliveryTimeMinutes(0.0)
                        .build());

        if ("CANCELLED".equals(event.getStatus())) {
            delivery.setFailedDeliveries(delivery.getFailedDeliveries() + 1);
        } else {
            delivery.setTotalDeliveries(delivery.getTotalDeliveries() + 1);
            if (event.isOnTime()) {
                delivery.setOnTimeDeliveries(delivery.getOnTimeDeliveries() + 1);
            } else {
                delivery.setLateDeliveries(delivery.getLateDeliveries() + 1);
            }

            if (event.getDeliveryTimeMinutes() != null) {
                double current = delivery.getAverageDeliveryTimeMinutes();
                long count = delivery.getTotalDeliveries();
                delivery.setAverageDeliveryTimeMinutes(
                        ((current * (count - 1)) + event.getDeliveryTimeMinutes()) / count);
            }
        }

        double slaBreachRate = delivery.getTotalDeliveries() > 0
                ? (delivery.getLateDeliveries() * 100.0) / delivery.getTotalDeliveries() : 0.0;
        delivery.setSlaBreachRate(slaBreachRate);
        delivery.setUpdatedAt(LocalDateTime.now());
        deliveryRepo.save(delivery);
    }
}
