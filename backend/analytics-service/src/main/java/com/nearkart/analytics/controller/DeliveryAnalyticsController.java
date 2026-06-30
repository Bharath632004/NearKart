package com.nearkart.analytics.controller;

import com.nearkart.analytics.model.DeliveryAnalytics;
import com.nearkart.analytics.repository.DeliveryAnalyticsRepository;
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

    private final DeliveryAnalyticsRepository deliveryRepo;

    @GetMapping("/sla")
    public ResponseEntity<List<DeliveryAnalytics>> getSlaReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(deliveryRepo.findByDateBetweenOrderByDateAsc(from, to));
    }

    @GetMapping("/today")
    public ResponseEntity<DeliveryAnalytics> getTodayDelivery() {
        return deliveryRepo.findByDate(LocalDate.now())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
