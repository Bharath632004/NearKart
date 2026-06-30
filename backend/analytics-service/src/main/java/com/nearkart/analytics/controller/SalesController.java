package com.nearkart.analytics.controller;

import com.nearkart.analytics.model.SalesAnalytics;
import com.nearkart.analytics.service.SalesAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesAnalyticsService salesService;

    @GetMapping("/daily")
    public ResponseEntity<SalesAnalytics> getDailySales(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().toString()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        SalesAnalytics result = salesService.getDailySales(date);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping("/range")
    public ResponseEntity<List<SalesAnalytics>> getSalesRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAILY") SalesAnalytics.Period period) {
        return ResponseEntity.ok(salesService.getSalesByRange(period, from, to));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<SalesAnalytics>> getLatestSales(
            @RequestParam(defaultValue = "WEEKLY") SalesAnalytics.Period period,
            @RequestParam(defaultValue = "7") int count) {
        return ResponseEntity.ok(salesService.getLatestSales(period, count));
    }
}
