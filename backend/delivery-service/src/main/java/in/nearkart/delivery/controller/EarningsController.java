package in.nearkart.delivery.controller;

import in.nearkart.delivery.dto.response.ApiResponse;
import in.nearkart.delivery.dto.response.EarningResponse;
import in.nearkart.delivery.service.DeliveryEarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery/earnings")
@RequiredArgsConstructor
public class EarningsController {

    private final DeliveryEarningService earningService;

    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<Page<EarningResponse>> getEarnings(
            @PathVariable UUID partnerId, Pageable pageable) {
        return ResponseEntity.ok(earningService.getEarningsByPartner(partnerId, pageable));
    }

    @GetMapping("/partner/{partnerId}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(@PathVariable UUID partnerId) {
        return ResponseEntity.ok(ApiResponse.success(earningService.getUnsettledBalance(partnerId)));
    }

    @PostMapping("/partner/{partnerId}/settle")
    public ResponseEntity<ApiResponse<String>> settle(@PathVariable UUID partnerId) {
        earningService.settleEarnings(partnerId);
        return ResponseEntity.ok(ApiResponse.success("Earnings settled successfully"));
    }
}
