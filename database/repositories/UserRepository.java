package com.nearkart.database.repositories;

import com.nearkart.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity.
 * Soft-delete aware. All login lookups check is_enabled and deleted_at.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // Soft delete
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void softDelete(@Param("id") UUID id);

    // Lock account
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isLocked = :locked WHERE u.id = :id")
    void setLocked(@Param("id") UUID id, @Param("locked") boolean locked);

    // Enable account (post OTP verification)
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isEnabled = TRUE WHERE u.id = :id")
    void enableUser(@Param("id") UUID id);
}
