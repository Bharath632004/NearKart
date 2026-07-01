package in.nearkart.payment.controller;

import in.nearkart.payment.dto.request.WalletTopUpRequest;
import in.nearkart.payment.dto.response.ApiResponse;
import in.nearkart.payment.dto.response.WalletResponse;
import in.nearkart.payment.entity.WalletTransaction;
import in.nearkart.payment.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Get or create the wallet for the current user.
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @RequestHeader("X-User-Id") UUID userId) {
        WalletResponse wallet = walletService.getOrCreateWallet(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Wallet fetched", wallet));
    }

    /**
     * Top-up the wallet.
     */
    @PostMapping("/top-up")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<WalletResponse>> topUp(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody WalletTopUpRequest request) {
        WalletResponse wallet = walletService.topUp(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Wallet top-up successful", wallet));
    }

    /**
     * Get paginated transaction history for the wallet.
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<WalletTransaction>>> getTransactions(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<WalletTransaction> txPage =
                walletService.getTransactionHistory(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(
                ApiResponse.success("Transactions fetched", txPage));
    }
}
