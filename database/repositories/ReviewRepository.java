package com.nearkart.database.repositories;

import com.nearkart.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, UUID entityId, Pageable pageable);

    Optional<Review> findByReviewerIdAndEntityTypeAndEntityId(
            UUID reviewerId, String entityType, UUID entityId);

    // Average rating for an entity
    @Query(value = "SELECT COALESCE(AVG(rating), 0) FROM reviews "
                 + "WHERE entity_type = :type AND entity_id = :id",
           nativeQuery = true)
    Double getAverageRating(@Param("type") String type, @Param("id") UUID id);

    // Count ratings for an entity
    long countByEntityTypeAndEntityId(String entityType, UUID entityId);
}
