package in.nearkart.payment.service.impl;

import in.nearkart.payment.dto.request.WalletTopUpRequest;
import in.nearkart.payment.dto.response.WalletResponse;
import in.nearkart.payment.entity.*;
import in.nearkart.payment.exception.InsufficientWalletBalanceException;
import in.nearkart.payment.exception.WalletNotFoundException;
import in.nearkart.payment.repository.WalletRepository;
import in.nearkart.payment.repository.WalletTransactionRepository;
import in.nearkart.payment.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository            walletRepository;
    private final WalletTransactionRepository walletTxRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {
        Wallet wallet = findOrCreate(userId);
        return toResponse(wallet);
    }

    @Override
    public WalletResponse topUp(UUID userId, WalletTopUpRequest request) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> createWallet(userId));

        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.add(request.getAmount()));
        walletRepository.save(wallet);

        recordTransaction(wallet, WalletTxType.CREDIT, request.getAmount(),
                before, wallet.getBalance(), "Wallet top-up", null);

        log.info("Wallet top-up: userId={}, amount={}, newBalance={}",
                userId, request.getAmount(), wallet.getBalance());
        return toResponse(wallet);
    }

    @Override
    public WalletResponse debit(UUID userId, BigDecimal amount,
                                String description, UUID referenceId) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + userId));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientWalletBalanceException(
                    "Insufficient wallet balance. Available: " + wallet.getBalance());
        }

        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.subtract(amount));
        walletRepository.save(wallet);

        recordTransaction(wallet, WalletTxType.DEBIT, amount,
                before, wallet.getBalance(), description, referenceId);

        log.info("Wallet debit: userId={}, amount={}, newBalance={}",
                userId, amount, wallet.getBalance());
        return toResponse(wallet);
    }

    @Override
    public WalletResponse credit(UUID userId, BigDecimal amount,
                                 String description, UUID referenceId) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> createWallet(userId));

        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.add(amount));
        walletRepository.save(wallet);

        recordTransaction(wallet, WalletTxType.CREDIT, amount,
                before, wallet.getBalance(), description, referenceId);

        return toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransaction> getTransactionHistory(UUID userId, Pageable pageable) {
        Wallet wallet = findOrCreate(userId);
        return walletTxRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);
    }

    // ----------------------------------------------------------------
    private void recordTransaction(Wallet wallet, WalletTxType type,
                                   BigDecimal amount, BigDecimal before,
                                   BigDecimal after, String desc, UUID refId) {
        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet).type(type).amount(amount)
                .balanceBefore(before).balanceAfter(after)
                .description(desc).referenceId(refId)
                .build();
        walletTxRepository.save(tx);
    }

    private Wallet findOrCreate(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> createWallet(userId));
    }

    private Wallet createWallet(UUID userId) {
        log.info("Creating new wallet for userId={}", userId);
        return walletRepository.save(Wallet.builder().userId(userId).build());
    }

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder()
                .id(w.getId()).userId(w.getUserId())
                .balance(w.getBalance()).isActive(w.getIsActive())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}
