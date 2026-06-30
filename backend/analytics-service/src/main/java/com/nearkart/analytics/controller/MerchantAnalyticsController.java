package com.nearkart.analytics.controller;

import com.nearkart.analytics.model.MerchantAnalytics;
import com.nearkart.analytics.service.MerchantAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/merchants")
@RequiredArgsConstructor
public class MerchantAnalyticsController {

    private final MerchantAnalyticsService merchantService;

    @GetMapping("/{merchantId}/report")
    public ResponseEntity<List<MerchantAnalytics>> getMerchantReport(
            @PathVariable String merchantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(merchantService.getMerchantReport(merchantId, from, to));
    }

    @GetMapping("/top")
    public ResponseEntity<List<MerchantAnalytics>> getTopMerchants(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(merchantService.getTopMerchantsByRevenue(targetDate));
    }
}
