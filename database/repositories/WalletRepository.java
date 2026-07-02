package com.nearkart.database.repositories;

import com.nearkart.domain.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Wallet entity.
 * All balance mutations use PESSIMISTIC_WRITE to prevent double-spend.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    Optional<Wallet> findByUserIdWithLock(@Param("userId") UUID userId);

    // Credit wallet (optimistic version check)
    @Modifying
    @Transactional
    @Query(value = "UPDATE wallets SET balance = balance + :amount, "
                 + "version = version + 1, updated_at = NOW() "
                 + "WHERE user_id = :userId",
           nativeQuery = true)
    void credit(@Param("userId") UUID userId, @Param("amount") BigDecimal amount);

    // Debit wallet – check sufficient balance
    @Modifying
    @Transactional
    @Query(value = "UPDATE wallets SET balance = balance - :amount, "
                 + "version = version + 1, updated_at = NOW() "
                 + "WHERE user_id = :userId AND balance >= :amount",
           nativeQuery = true)
    int debit(@Param("userId") UUID userId, @Param("amount") BigDecimal amount);
}
