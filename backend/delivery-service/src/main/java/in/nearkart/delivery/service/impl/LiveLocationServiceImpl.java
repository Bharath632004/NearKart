package in.nearkart.delivery.service.impl;

import in.nearkart.delivery.dto.request.UpdateLocationRequest;
import in.nearkart.delivery.dto.response.LocationResponse;
import in.nearkart.delivery.entity.LiveLocation;
import in.nearkart.delivery.exception.AssignmentNotFoundException;
import in.nearkart.delivery.repository.DeliveryAssignmentRepository;
import in.nearkart.delivery.repository.DeliveryPartnerRepository;
import in.nearkart.delivery.repository.LiveLocationRepository;
import in.nearkart.delivery.service.LiveLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LiveLocationServiceImpl implements LiveLocationService {

    private final LiveLocationRepository locationRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryAssignmentRepository assignmentRepository;

    @Override
    public LocationResponse updateLocation(UUID partnerId, UUID assignmentId, UpdateLocationRequest request) {
        LiveLocation location = LiveLocation.builder()
                .partnerId(partnerId)
                .assignmentId(assignmentId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speedKmph(request.getSpeedKmph())
                .headingDegrees(request.getHeadingDegrees())
                .build();

        LiveLocation saved = locationRepository.save(location);
        log.debug("Location updated: partnerId={}, lat={}, lng={}",
                partnerId, request.getLatitude(), request.getLongitude());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getLatestLocation(UUID assignmentId) {
        List<LiveLocation> locations = locationRepository.findLatestByAssignmentId(assignmentId);
        if (locations.isEmpty()) {
            throw new AssignmentNotFoundException("No location data yet for assignment: " + assignmentId);
        }
        return toResponse(locations.get(0));
    }

    private LocationResponse toResponse(LiveLocation l) {
        return LocationResponse.builder()
                .partnerId(l.getPartnerId())
                .assignmentId(l.getAssignmentId())
                .latitude(l.getLatitude())
                .longitude(l.getLongitude())
                .speedKmph(l.getSpeedKmph())
                .recordedAt(l.getRecordedAt())
                .build();
    }
}
