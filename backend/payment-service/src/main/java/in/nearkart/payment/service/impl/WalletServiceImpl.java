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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Wallet saved = walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .walletId(saved.getId())
                .amount(request.getAmount())
                .type(WalletTxType.CREDIT)
                .description("Wallet top-up via " + request.getSource())
                .createdAt(LocalDateTime.now())
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

        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet saved = walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .walletId(saved.getId())
                .amount(amount)
                .type(WalletTxType.DEBIT)
                .description(description)
                .createdAt(LocalDateTime.now())
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

        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet saved = walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .walletId(saved.getId())
                .amount(amount)
                .type(WalletTxType.CREDIT)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        walletTransactionRepository.save(tx);
        return toResponse(saved);
    }

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder()
                .id(w.getId())
                .customerId(w.getCustomerId())
                .balance(w.getBalance())
                .currency(w.getCurrency())
                .build();
    }
}
