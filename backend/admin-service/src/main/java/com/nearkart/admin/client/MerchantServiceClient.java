package com.nearkart.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "merchant-service")
public interface MerchantServiceClient {

    @PutMapping("/api/merchants/{merchantId}/approve")
    void approveMerchant(@PathVariable Long merchantId);

    @PutMapping("/api/merchants/{merchantId}/reject")
    void rejectMerchant(@PathVariable Long merchantId, @RequestParam String reason);

    @PutMapping("/api/merchants/{merchantId}/suspend")
    void suspendMerchant(@PathVariable Long merchantId, @RequestParam String reason);

    @GetMapping("/api/merchants/count")
    Long getMerchantCount();

    @GetMapping("/api/merchants/pending/count")
    Long getPendingApprovalCount();
}
