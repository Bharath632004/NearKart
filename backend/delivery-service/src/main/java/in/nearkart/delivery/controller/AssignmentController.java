package in.nearkart.delivery.controller;

import in.nearkart.delivery.dto.request.UpdateAssignmentStatusRequest;
import in.nearkart.delivery.dto.request.VerifyOtpRequest;
import in.nearkart.delivery.dto.response.ApiResponse;
import in.nearkart.delivery.dto.response.AssignmentResponse;
import in.nearkart.delivery.service.DeliveryAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final DeliveryAssignmentService assignmentService;

    /** GET /api/v1/delivery/assignments/{assignmentId} */
    @GetMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getById(
            @PathVariable UUID assignmentId) {
        return ResponseEntity.ok(ApiResponse.success("Assignment fetched",
                assignmentService.getById(assignmentId)));
    }

    /** GET /api/v1/delivery/assignments/order/{orderId} */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getByOrderId(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success("Assignment fetched",
                assignmentService.getByOrderId(orderId)));
    }

    /** PATCH /api/v1/delivery/assignments/{assignmentId}/status */
    @PatchMapping("/{assignmentId}/status")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateStatus(
            @PathVariable UUID assignmentId,
            @Valid @RequestBody UpdateAssignmentStatusRequest request,
            @RequestHeader("X-Partner-Id") UUID partnerId) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                assignmentService.updateStatus(assignmentId, request, partnerId)));
    }

    /** POST /api/v1/delivery/assignments/otp/verify-delivery */
    @PostMapping("/otp/verify-delivery")
    public ResponseEntity<ApiResponse<Boolean>> verifyDeliveryOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            @RequestHeader("X-Partner-Id") UUID partnerId) {
        boolean verified = assignmentService.verifyDeliveryOtp(request, partnerId);
        return ResponseEntity.ok(ApiResponse.success("OTP verified", verified));
    }

    /** POST /api/v1/delivery/assignments/otp/verify-pickup */
    @PostMapping("/otp/verify-pickup")
    public ResponseEntity<ApiResponse<Boolean>> verifyPickupOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            @RequestHeader("X-Partner-Id") UUID partnerId) {
        boolean verified = assignmentService.verifyPickupOtp(request, partnerId);
        return ResponseEntity.ok(ApiResponse.success("Pickup OTP verified", verified));
    }

    /** POST /api/v1/delivery/assignments/manual?orderId=...&partnerId=... (admin) */
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<AssignmentResponse>> manualAssign(
            @RequestParam UUID orderId,
            @RequestParam UUID partnerId) {
        return ResponseEntity.ok(ApiResponse.success("Manual assignment created",
                assignmentService.manualAssign(orderId, partnerId)));
    }

    /** GET /api/v1/delivery/assignments/partner/{partnerId}/history */
    @GetMapping("/partner/{partnerId}/history")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getPartnerHistory(
            @PathVariable UUID partnerId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("History fetched",
                assignmentService.getPartnerAssignments(partnerId, pageable)));
    }
}
