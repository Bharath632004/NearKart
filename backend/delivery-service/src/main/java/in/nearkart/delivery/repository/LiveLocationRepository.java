package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.LiveLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveLocationRepository extends JpaRepository<LiveLocation, UUID> {

    Optional<LiveLocation> findByPartnerId(UUID partnerId);

    Optional<LiveLocation> findByAssignmentId(UUID assignmentId);
}
