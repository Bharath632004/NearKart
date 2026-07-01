package com.nearkart.merchant.service;

import com.nearkart.merchant.dto.AdminAnalyticsResponse;
import com.nearkart.merchant.entity.Merchant;
import com.nearkart.merchant.entity.MerchantStatus;
import com.nearkart.merchant.exception.ResourceNotFoundException;
import com.nearkart.merchant.repository.MerchantRepository;
import com.nearkart.merchant.repository.ShopRepository;
import com.nearkart.merchant.repository.SettlementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void approveMerchant_shouldUpdateStatus_whenMerchantExists() {
        UUID id = UUID.randomUUID();
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setStatus(MerchantStatus.PENDING);

        when(merchantRepository.findById(id)).thenReturn(Optional.of(merchant));
        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);

        adminService.approveMerchant(id);

        assertThat(merchant.getStatus()).isEqualTo(MerchantStatus.APPROVED);
        verify(merchantRepository).save(merchant);
    }

    @Test
    void approveMerchant_shouldThrow_whenMerchantNotFound() {
        UUID id = UUID.randomUUID();
        when(merchantRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.approveMerchant(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectMerchant_shouldUpdateStatus_whenMerchantExists() {
        UUID id = UUID.randomUUID();
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setStatus(MerchantStatus.PENDING);

        when(merchantRepository.findById(id)).thenReturn(Optional.of(merchant));
        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);

        adminService.rejectMerchant(id, "Invalid documents");

        assertThat(merchant.getStatus()).isEqualTo(MerchantStatus.REJECTED);
        verify(merchantRepository).save(merchant);
    }

    @Test
    void getAdminAnalytics_shouldReturnStats() {
        when(merchantRepository.count()).thenReturn(10L);
        when(merchantRepository.countByStatus(MerchantStatus.APPROVED)).thenReturn(7L);
        when(merchantRepository.countByStatus(MerchantStatus.PENDING)).thenReturn(3L);

        AdminAnalyticsResponse response = adminService.getAdminAnalytics();

        assertThat(response.getTotalMerchants()).isEqualTo(10L);
        assertThat(response.getApprovedMerchants()).isEqualTo(7L);
    }
}
