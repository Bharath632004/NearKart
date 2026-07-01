package in.nearkart.notification.repository;

import in.nearkart.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    // userId stored as String
    List<DeviceToken> findByUserIdAndActiveTrue(String userId);

    // UUID overload used by FcmService and NotificationServiceImpl
    default List<DeviceToken> findByUserIdAndIsActiveTrue(UUID userId) {
        return findByUserIdAndActiveTrue(userId.toString());
    }

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    // Used by FcmService, NotificationServiceImpl: soft-deactivate token
    @Modifying
    @Transactional
    @Query("UPDATE DeviceToken dt SET dt.active = false WHERE dt.fcmToken = :token")
    void deactivateByToken(@Param("token") String token);

    void deleteByFcmToken(String fcmToken);
}
