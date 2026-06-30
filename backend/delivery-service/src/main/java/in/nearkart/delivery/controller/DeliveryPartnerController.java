package in.nearkart.delivery.controller;

import in.nearkart.delivery.dto.request.RegisterPartnerRequest;
import in.nearkart.delivery.dto.request.UpdateLocationRequest;
import in.nearkart.delivery.dto.response.ApiResponse;
import in.nearkart.delivery.dto.response.PartnerResponse;
import in.nearkart.delivery.entity.PartnerStatus;
import in.nearkart.delivery.service.DeliveryPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery/partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService partnerService;

    /** POST /api/v1/delivery/partners/register */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PartnerResponse>> register(
            @Valid @RequestBody RegisterPartnerRequest request) {
        PartnerResponse response = partnerService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Partner registered successfully. Pending KYC.", response));
    }

    /** GET /api/v1/delivery/partners/{partnerId} */
    @GetMapping("/{partnerId}")
    public ResponseEntity<ApiResponse<PartnerResponse>> getPartner(
            @PathVariable UUID partnerId) {
        return ResponseEntity.ok(ApiResponse.success("Partner fetched", partnerService.getById(partnerId)));
    }

    /** PATCH /api/v1/delivery/partners/{partnerId}/status?status=ONLINE */
    @PatchMapping("/{partnerId}/status")
    public ResponseEntity<ApiResponse<PartnerResponse>> updateStatus(
            @PathVariable UUID partnerId,
            @RequestParam PartnerStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                partnerService.updateStatus(partnerId, status)));
    }

    /** PATCH /api/v1/delivery/partners/{partnerId}/location */
    @PatchMapping("/{partnerId}/location")
    public ResponseEntity<ApiResponse<PartnerResponse>> updateLocation(
            @PathVariable UUID partnerId,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Location updated",
                partnerService.updateLocation(partnerId, request)));
    }

    /** POST /api/v1/delivery/partners/{partnerId}/kyc/approve  (admin only) */
    @PostMapping("/{partnerId}/kyc/approve")
    public ResponseEntity<ApiResponse<Void>> approveKyc(@PathVariable UUID partnerId) {
        partnerService.approveKyc(partnerId);
        return ResponseEntity.ok(ApiResponse.success("KYC approved", null));
    }

    /** GET /api/v1/delivery/partners?status=ONLINE (admin) */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartnerResponse>>> listPartners(
            @RequestParam(required = false) PartnerStatus status,
            Pageable pageable) {
        Page<PartnerResponse> page = status != null
                ? partnerService.listByStatus(status, pageable)
                : partnerService.listAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Partners fetched", page));
    }
}
