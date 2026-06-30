package in.nearkart.delivery.controller;

import in.nearkart.delivery.dto.response.ApiResponse;
import in.nearkart.delivery.dto.response.LocationResponse;
import in.nearkart.delivery.service.LiveLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final LiveLocationService locationService;

    /**
     * REST fallback for customers who cannot use WebSocket.
     * GET /api/v1/delivery/tracking/{assignmentId}/latest
     */
    @GetMapping("/{assignmentId}/latest")
    public ResponseEntity<ApiResponse<LocationResponse>> getLatestLocation(
            @PathVariable UUID assignmentId) {
        return ResponseEntity.ok(ApiResponse.success("Latest location fetched",
                locationService.getLatestLocation(assignmentId)));
    }
}
