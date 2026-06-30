package in.nearkart.notification.repository;

import in.nearkart.notification.entity.NotificationChannel;
import in.nearkart.notification.entity.NotificationLog;
import in.nearkart.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Page<NotificationLog> findByUserId(String userId, Pageable pageable);
    List<NotificationLog> findByUserIdAndChannel(String userId, NotificationChannel channel);
    List<NotificationLog> findByStatus(NotificationStatus status);
    long countByUserIdAndCreatedAtBetween(String userId, LocalDateTime from, LocalDateTime to);
}
