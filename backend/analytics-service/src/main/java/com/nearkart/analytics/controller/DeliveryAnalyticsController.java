package com.nearkart.analytics.controller;

import com.nearkart.analytics.model.DeliveryAnalytics;
import com.nearkart.analytics.service.DeliveryAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/delivery")
@RequiredArgsConstructor
public class DeliveryAnalyticsController {

    private final DeliveryAnalyticsService deliveryService;

    @GetMapping("/sla")
    public ResponseEntity<List<DeliveryAnalytics>> getSlaReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(deliveryService.getSlaReport(from, to));
    }

    @GetMapping("/today")
    public ResponseEntity<DeliveryAnalytics> getTodayDelivery() {
        DeliveryAnalytics result = deliveryService.getToday();
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }
}
