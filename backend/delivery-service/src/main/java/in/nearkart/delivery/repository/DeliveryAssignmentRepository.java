import in.nearkart.delivery.entity.LiveLocation;
import java.util.Optional;
import java.util.UUID;

public Optional<LiveLocation> getLatestLocation(UUID assignmentId) {
    return locationRepository.findLatestByAssignmentId(assignmentId);
}
