package in.nearkart.notification.repository;

import in.nearkart.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

    List<DeviceToken> findByUserIdAndIsActiveTrue(UUID userId);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.isActive = false WHERE d.token = :token")
    void deactivateByToken(String token);
}
