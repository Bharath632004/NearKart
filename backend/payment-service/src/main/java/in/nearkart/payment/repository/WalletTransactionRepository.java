package in.nearkart.payment.repository;

import in.nearkart.payment.entity.WalletTransaction;
import in.nearkart.payment.entity.WalletTxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    // Non-paginated version (existing)
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId);

    // Paginated version used by WalletServiceImpl.getTransactionHistory()
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    List<WalletTransaction> findByWalletIdAndType(UUID walletId, WalletTxType type);
}
