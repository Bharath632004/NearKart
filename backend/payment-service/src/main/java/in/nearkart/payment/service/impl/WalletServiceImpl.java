package in.nearkart.payment.service.impl;

import in.nearkart.payment.dto.request.WalletTopUpRequest;
import in.nearkart.payment.dto.response.WalletResponse;
import in.nearkart.payment.entity.*;
import in.nearkart.payment.exception.PaymentException;
import in.nearkart.payment.exception.PaymentNotFoundException;
import in.nearkart.payment.repository.WalletRepository;
import in.nearkart.payment.repository.WalletTransactionRepository;
import in.nearkart.payment.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public WalletResponse getOrCreateWallet(UUID customerId) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Wallet w = Wallet.builder()
                            .customerId(customerId)
                            .balance(BigDecimal.ZERO)
                            .currency("INR")
                            .build();
                    return walletRepository.save(w);
                });
        return toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByCustomerId(UUID customerId) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new PaymentNotFoundException("Wallet not found for customer: " + customerId));
        return toResponse(wallet);
    }

    @Override
    public WalletResponse topUp(UUID customerId, WalletTopUpRequest request) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .customerId(customerId)
                        .balance(BigDecimal.ZERO)
                        .currency("INR")
                        .build()));

        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.add(request.getAmount()));
        Wallet saved = walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .walletId(saved.getId())
                .amount(request.getAmount())
                .type(WalletTxType.CREDIT)
                .balanceBefore(before)
                .balanceAfter(saved.getBalance())
                .description("Wallet top-up via " + (request.getSource() != null ? request.getSource() : request.getRazorpayPaymentId()))
                .build();
        walletTransactionRepository.save(tx);

        log.info("Wallet top-up: customerId={}, amount={}, newBalance={}",
                customerId, request.getAmount(), saved.getBalance());
        return toResponse(saved);
    }

    @Override
    public WalletResponse debit(UUID customerId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new PaymentNotFoundException("Wallet not found: " + customerId));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new PaymentException("Insufficient wallet balance. Available: " + wallet.getBalance());
        }

        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.subtract(amount));
        Wallet saved = walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .walletId(saved.getId())
                .amount(amount)
                .type(WalletTxType.DEBIT)
                .balanceBefore(before)
                .balanceAfter(saved.getBalance())
                .description(description)
                .build();
        walletTransactionRepository.save(tx);

        log.info("Wallet debit: customerId={}, amount={}, newBalance={}",
                customerId, amount, saved.getBalance());
        return toResponse(saved);
    }

    @Override
    public WalletResponse credit(UUID customerId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .customerId(customerId).balance(BigDecimal.ZERO).currency("INR").build()));

        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.add(amount));
        Wallet saved = walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .walletId(saved.getId())
                .amount(amount)
                .type(WalletTxType.CREDIT)
                .balanceBefore(before)
                .balanceAfter(saved.getBalance())
                .description(description)
                .build();
        walletTransactionRepository.save(tx);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransaction> getTransactionHistory(UUID customerId, PageRequest pageRequest) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new PaymentNotFoundException("Wallet not found for customer: " + customerId));
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageRequest);
    }

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder()
                .id(w.getId())
                .userId(w.getCustomerId())
                .balance(w.getBalance())
                .isActive(w.getIsActive())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}
