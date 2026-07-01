package in.nearkart.payment.service;

import in.nearkart.payment.dto.request.WalletTopUpRequest;
import in.nearkart.payment.dto.response.WalletResponse;
import in.nearkart.payment.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {
    WalletResponse getOrCreateWallet(UUID customerId);
    WalletResponse getWalletByCustomerId(UUID customerId);
    WalletResponse topUp(UUID customerId, WalletTopUpRequest request);
    WalletResponse debit(UUID customerId, BigDecimal amount, String description);
    WalletResponse credit(UUID customerId, BigDecimal amount, String description);
    Page<WalletTransaction> getTransactionHistory(UUID customerId, PageRequest pageRequest);
}
