package com.nearkart.repository;

import com.nearkart.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {

    /** Active subscription for a user */
    @Query("""
            SELECT s FROM UserSubscription s
            WHERE s.userId = :userId
              AND s.isActive = true
              AND s.expiresAt > :now
            ORDER BY s.expiresAt DESC
            """)
    Optional<UserSubscription> findActiveByUser(
            @Param("userId") UUID userId,
            @Param("now")    LocalDateTime now);

    /** Subscriptions expiring soon (for renewal notifications) */
    @Query("""
            SELECT s FROM UserSubscription s
            WHERE s.isActive = true
              AND s.expiresAt BETWEEN :now AND :threshold
            """)
    List<UserSubscription> findExpiringSoon(
            @Param("now")       LocalDateTime now,
            @Param("threshold") LocalDateTime threshold);
}
