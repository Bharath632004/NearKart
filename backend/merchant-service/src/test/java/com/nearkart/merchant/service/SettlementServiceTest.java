package com.nearkart.merchant.service;

import com.nearkart.merchant.dto.SettlementRequest;
import com.nearkart.merchant.dto.SettlementResponse;
import com.nearkart.merchant.entity.Merchant;
import com.nearkart.merchant.entity.Settlement;
import com.nearkart.merchant.repository.MerchantRepository;
import com.nearkart.merchant.repository.SettlementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantService merchantService;

    @Test
    void createSettlement_shouldSucceed() {
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);

        SettlementRequest request = new SettlementRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setReferenceId("REF123");

        Settlement saved = new Settlement();
        saved.setId(UUID.randomUUID());
        saved.setAmount(new BigDecimal("5000.00"));
        saved.setMerchant(merchant);

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(settlementRepository.save(any(Settlement.class))).thenReturn(saved);

        SettlementResponse response = merchantService.createSettlement(merchantId, request);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo("5000.00");
    }

    @Test
    void getSettlementsByMerchant_shouldReturnList() {
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);

        Settlement s1 = new Settlement();
        s1.setId(UUID.randomUUID());
        s1.setAmount(BigDecimal.TEN);
        s1.setMerchant(merchant);

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(settlementRepository.findByMerchant(merchant)).thenReturn(List.of(s1));

        List<SettlementResponse> result = merchantService.getSettlementsByMerchant(merchantId);

        assertThat(result).hasSize(1);
    }
}
