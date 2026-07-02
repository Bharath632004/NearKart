package com.nearkart.repository.ai;

import com.nearkart.entity.ai.AiFraudFlag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AI fraud detection flags.
 */
@Repository
public interface AiFraudFlagRepository extends JpaRepository<AiFraudFlag, UUID> {

    /** All unreviewed flags above a given risk score */
    @Query("""
            SELECT f FROM AiFraudFlag f
            WHERE f.isReviewed = false
              AND f.riskScore >= :minScore
            ORDER BY f.riskScore DESC
            """)
    Page<AiFraudFlag> findUnreviewedHighRisk(
            @Param("minScore") BigDecimal minScore,
            Pageable pageable);

    /** Flags by entity (e.g., ORDER or PAYMENT) */
    List<AiFraudFlag> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, UUID entityId);

    /** Count critical unreviewed flags */
    long countByRiskLevelAndIsReviewedFalse(String riskLevel);
}
