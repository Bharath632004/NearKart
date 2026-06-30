package in.nearkart.notification.repository;

import in.nearkart.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUserIdAndActiveTrue(String userId);
    Optional<DeviceToken> findByFcmToken(String fcmToken);
    void deleteByFcmToken(String fcmToken);
}
