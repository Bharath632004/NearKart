package com.nearkart.merchant.repository;

import com.nearkart.merchant.entity.Merchant;
import com.nearkart.merchant.entity.MerchantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> findByUserId(UUID userId);
    Optional<Merchant> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUserId(UUID userId);
    long countByStatus(MerchantStatus status);
}
