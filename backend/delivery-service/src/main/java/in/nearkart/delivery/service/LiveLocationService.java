package in.nearkart.delivery.service;

import in.nearkart.delivery.dto.request.UpdateLocationRequest;
import in.nearkart.delivery.dto.response.LocationResponse;

import java.util.UUID;

public interface LiveLocationService {
    // Full 3-arg version (used by REST controller)
    LocationResponse updateLocation(UUID partnerId, UUID assignmentId, UpdateLocationRequest request);

    // 2-arg version used by WebSocket handler (no assignmentId available in WS context)
    LocationResponse updateLocation(UUID partnerId, UpdateLocationRequest request);

    LocationResponse getLatestLocation(UUID assignmentId);
}
