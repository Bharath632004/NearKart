package in.nearkart.payment.service;

import in.nearkart.payment.dto.request.WalletTopUpRequest;
import in.nearkart.payment.dto.response.WalletResponse;
import in.nearkart.payment.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    // Get wallet if exists, otherwise create a new one
    WalletResponse getOrCreateWallet(UUID customerId);

    // Get wallet by customerId (no auto-create)
    WalletResponse getWalletByCustomerId(UUID customerId);

    // Top-up the wallet
    WalletResponse topUp(UUID customerId, WalletTopUpRequest request);

    // Debit the wallet
    WalletResponse debit(UUID customerId, BigDecimal amount, String description);

    // Credit the wallet
    WalletResponse credit(UUID customerId, BigDecimal amount, String description);

    // Used by WalletController for transaction history
    Page<WalletTransaction> getTransactionHistory(UUID customerId, PageRequest pageRequest);
}
