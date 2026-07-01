package com.nearkart.user.repository;

import com.nearkart.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserProfileUserId(UUID userId);

    // Updated JPQL field name from isDefault -> defaultAddress to match renamed entity field
    @Modifying
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.userProfile.userId = :userId")
    void clearDefaultForUser(UUID userId);
}
