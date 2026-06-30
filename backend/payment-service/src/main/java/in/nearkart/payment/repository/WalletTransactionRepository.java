package in.nearkart.payment.repository;

import in.nearkart.payment.entity.WalletTransaction;
import in.nearkart.payment.entity.WalletTxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
    List<WalletTransaction> findByWalletIdAndType(UUID walletId, WalletTxType type);
}
