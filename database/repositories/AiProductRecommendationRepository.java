package com.nearkart.repository.ai;

import com.nearkart.entity.ai.AiProductRecommendation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AI product recommendations.
 * Supports retrieval of fresh, user-specific ranked suggestions.
 */
@Repository
public interface AiProductRecommendationRepository
        extends JpaRepository<AiProductRecommendation, UUID> {

    /** Top-N valid (non-expired) recommendations for a user, ordered by score DESC */
    @Query("""
            SELECT r FROM AiProductRecommendation r
            WHERE r.userId = :userId
              AND r.expiresAt > :now
            ORDER BY r.score DESC
            """)
    List<AiProductRecommendation> findTopByUserIdAndValid(
            @Param("userId") UUID userId,
            @Param("now")    LocalDateTime now,
            Pageable pageable);

    /** Mark recommendation as clicked */
    @Modifying
    @Transactional
    @Query("UPDATE AiProductRecommendation r SET r.isClicked = true WHERE r.id = :id")
    void markClicked(@Param("id") UUID id);

    /** Mark recommendation as purchased */
    @Modifying
    @Transactional
    @Query("UPDATE AiProductRecommendation r SET r.isPurchased = true WHERE r.id = :id")
    void markPurchased(@Param("id") UUID id);

    /** Purge expired recommendations (scheduled job) */
    @Modifying
    @Transactional
    @Query("DELETE FROM AiProductRecommendation r WHERE r.expiresAt < :now")
    int purgeExpired(@Param("now") LocalDateTime now);
}
