package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.LiveLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LiveLocationRepository extends JpaRepository<LiveLocation, UUID> {

    @Query("SELECT l FROM LiveLocation l WHERE l.assignmentId = :assignmentId ORDER BY l.recordedAt DESC")
    List<LiveLocation> findLatestByAssignmentId(@Param("assignmentId") UUID assignmentId);
}
