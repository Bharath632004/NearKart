package com.nearkart.analytics.controller;

import com.nearkart.analytics.model.CustomerAnalytics;
import com.nearkart.analytics.service.CustomerAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/customers")
@RequiredArgsConstructor
public class CustomerAnalyticsController {

    private final CustomerAnalyticsService customerService;

    @GetMapping("/today")
    public ResponseEntity<CustomerAnalytics> getToday() {
        CustomerAnalytics result = customerService.getByDate(LocalDate.now());
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<CustomerAnalytics> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CustomerAnalytics result = customerService.getByDate(date);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping("/range")
    public ResponseEntity<List<CustomerAnalytics>> getRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(customerService.getByRange(from, to));
    }
}
