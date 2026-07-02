package com.nearkart.merchant.service;

import com.nearkart.merchant.dto.MerchantRegistrationRequest;
import com.nearkart.merchant.dto.MerchantResponse;
import com.nearkart.merchant.entity.Merchant;
import com.nearkart.merchant.entity.MerchantStatus;
import com.nearkart.merchant.exception.MerchantAlreadyExistsException;
import com.nearkart.merchant.exception.ResourceNotFoundException;
import com.nearkart.merchant.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock private MerchantRepository merchantRepository;
    @Mock private KycDocumentRepository kycDocumentRepository;
    @Mock private ShopRepository shopRepository;
    @Mock private PromotionRepository promotionRepository;
    @Mock private SettlementRepository settlementRepository;
    @Mock private S3Service s3Service;

    @InjectMocks
    private MerchantService merchantService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(merchantService, "kycRequiredDocs", 2);
    }

    // ── Test 1: registerMerchant succeeds ────────────────────────────────────────
    @Test
    void registerMerchant_success() {
        UUID userId = UUID.randomUUID();
        MerchantRegistrationRequest req = new MerchantRegistrationRequest();
        req.setBusinessName("Test Biz");
        req.setBusinessType("RETAIL");
        req.setEmail("biz@test.com");
        req.setPhone("9876543210");

        when(merchantRepository.existsByUserId(userId)).thenReturn(false);
        when(merchantRepository.existsByEmail(req.getEmail())).thenReturn(false);

        Merchant saved = Merchant.builder()
                .id(UUID.randomUUID()).userId(userId)
                .businessName("Test Biz").businessType("RETAIL")
                .email("biz@test.com").phone("9876543210")
                .status(MerchantStatus.PENDING_KYC)
                .build();
        when(merchantRepository.save(any(Merchant.class))).thenReturn(saved);

        MerchantResponse response = merchantService.registerMerchant(userId, req);

        assertThat(response.getStatus()).isEqualTo(MerchantStatus.PENDING_KYC);
        assertThat(response.getEmail()).isEqualTo("biz@test.com");
    }

    // ── Test 2: registerMerchant duplicate userId throws ─────────────────────────
    @Test
    void registerMerchant_duplicateUserId_throws() {
        UUID userId = UUID.randomUUID();
        MerchantRegistrationRequest req = new MerchantRegistrationRequest();
        req.setEmail("biz@test.com");

        when(merchantRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> merchantService.registerMerchant(userId, req))
                .isInstanceOf(MerchantAlreadyExistsException.class)
                .hasMessageContaining("already registered");
    }

    // ── Test 3: registerMerchant duplicate email throws ──────────────────────────
    @Test
    void registerMerchant_duplicateEmail_throws() {
        UUID userId = UUID.randomUUID();
        MerchantRegistrationRequest req = new MerchantRegistrationRequest();
        req.setEmail("dup@test.com");

        when(merchantRepository.existsByUserId(userId)).thenReturn(false);
        when(merchantRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> merchantService.registerMerchant(userId, req))
                .isInstanceOf(MerchantAlreadyExistsException.class)
                .hasMessageContaining("Email already registered");
    }

    // ── Test 4: getMerchantByUserId success ──────────────────────────────────────
    @Test
    void getMerchantByUserId_success() {
        UUID userId = UUID.randomUUID();
        Merchant m = Merchant.builder().id(UUID.randomUUID()).userId(userId)
                .businessName("Shop A").businessType("GROCERY")
                .email("a@b.com").phone("9000000000")
                .status(MerchantStatus.ACTIVE).build();

        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.of(m));

        MerchantResponse resp = merchantService.getMerchantByUserId(userId);
        assertThat(resp.getStatus()).isEqualTo(MerchantStatus.ACTIVE);
    }

    // ── Test 5: getMerchantByUserId not found throws ─────────────────────────────
    @Test
    void getMerchantByUserId_notFound_throws() {
        UUID userId = UUID.randomUUID();
        when(merchantRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> merchantService.getMerchantByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Merchant not found for user");
    }

    // ── Test 6: verifyKycDocument promotes merchant to ACTIVE when threshold met ─
    @Test
    void verifyKycDocument_promotesToActive_whenThresholdMet() {
        UUID docId = UUID.randomUUID();
        Merchant merchant = Merchant.builder().id(UUID.randomUUID())
                .status(MerchantStatus.KYC_SUBMITTED).build();

        com.nearkart.merchant.entity.KycDocument doc =
                com.nearkart.merchant.entity.KycDocument.builder()
                .id(docId).merchant(merchant)
                .documentType("AADHAAR").documentUrl("https://s3/key")
                .s3Key("kyc/key").verified(false).build();

        when(kycDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(kycDocumentRepository.save(any())).thenReturn(doc);
        when(kycDocumentRepository.countByMerchantIdAndVerified(merchant.getId(), true)).thenReturn(2L);
        when(merchantRepository.save(any())).thenReturn(merchant);

        merchantService.verifyKycDocument(docId, true, null);

        assertThat(merchant.getStatus()).isEqualTo(MerchantStatus.ACTIVE);
    }

    // ── Test 7: verifyKycDocument does NOT promote when below threshold ───────────
    @Test
    void verifyKycDocument_doesNotPromote_whenBelowThreshold() {
        UUID docId = UUID.randomUUID();
        Merchant merchant = Merchant.builder().id(UUID.randomUUID())
                .status(MerchantStatus.KYC_SUBMITTED).build();

        com.nearkart.merchant.entity.KycDocument doc =
                com.nearkart.merchant.entity.KycDocument.builder()
                .id(docId).merchant(merchant)
                .documentType("PAN").documentUrl("https://s3/key")
                .s3Key("kyc/key").verified(false).build();

        when(kycDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(kycDocumentRepository.save(any())).thenReturn(doc);
        when(kycDocumentRepository.countByMerchantIdAndVerified(merchant.getId(), true)).thenReturn(1L);

        merchantService.verifyKycDocument(docId, true, null);

        assertThat(merchant.getStatus()).isEqualTo(MerchantStatus.KYC_SUBMITTED);
        verify(merchantRepository, never()).save(any());
    }
}
