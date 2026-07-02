package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.LiveLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveLocationRepository extends JpaRepository<LiveLocation, UUID> {

    Optional<LiveLocation> findByPartnerId(UUID partnerId);

    /** Called at LiveLocationServiceImpl line 55 – returns the most recent location for an assignment */
    @Query("SELECT l FROM LiveLocation l WHERE l.assignmentId = :assignmentId ORDER BY l.recordedAt DESC")
    Optional<LiveLocation> findLatestByAssignmentId(@Param("assignmentId") UUID assignmentId);
}
