package com.nearkart.repository;

import com.nearkart.entity.UserDeviceToken;
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
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, UUID> {

    List<UserDeviceToken> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<UserDeviceToken> findByDeviceToken(String deviceToken);

    @Modifying
    @Transactional
    @Query("UPDATE UserDeviceToken t SET t.isActive = false WHERE t.userId = :userId")
    void deactivateAllForUser(@Param("userId") UUID userId);
}
