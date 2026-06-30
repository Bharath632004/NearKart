package in.nearkart.payment.service;

import in.nearkart.payment.dto.request.WalletTopUpRequest;
import in.nearkart.payment.dto.response.WalletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import in.nearkart.payment.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {
    WalletResponse getWallet(UUID userId);
    WalletResponse topUp(UUID userId, WalletTopUpRequest request);
    WalletResponse debit(UUID userId, BigDecimal amount, String description, UUID referenceId);
    WalletResponse credit(UUID userId, BigDecimal amount, String description, UUID referenceId);
    Page<WalletTransaction> getTransactionHistory(UUID userId, Pageable pageable);
}
