package in.nearkart.delivery.service;

import in.nearkart.delivery.dto.request.UpdateLocationRequest;
import in.nearkart.delivery.dto.response.LocationResponse;

import java.util.UUID;

public interface LiveLocationService {
    LocationResponse updateLocation(UUID partnerId, UUID assignmentId, UpdateLocationRequest request);
    LocationResponse getLatestLocation(UUID assignmentId);
}
