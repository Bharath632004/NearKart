package com.nearkart.database.repositories;

import com.nearkart.domain.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(UUID userId);

    // Mark all as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = TRUE WHERE n.userId = :userId AND n.isRead = FALSE")
    int markAllRead(@Param("userId") UUID userId);

    // Delete old read notifications (cleanup job)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM notifications WHERE user_id = :userId AND is_read = TRUE "
                 + "AND created_at < NOW() - INTERVAL '30 days'",
           nativeQuery = true)
    int deleteOldReadNotifications(@Param("userId") UUID userId);
}
