package in.nearkart.payment.controller;

import in.nearkart.payment.dto.request.RefundRequest;
import in.nearkart.payment.dto.response.ApiResponse;
import in.nearkart.payment.dto.response.RefundResponse;
import in.nearkart.payment.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RefundResponse>> initiateRefund(
            @Valid @RequestBody RefundRequest request) {
        RefundResponse resp = refundService.initiateRefund(request);
        return ResponseEntity.ok(ApiResponse.success("Refund initiated", resp));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefunds(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(
                ApiResponse.success("Refunds fetched", refundService.getRefundsByOrderId(orderId)));
    }
}
