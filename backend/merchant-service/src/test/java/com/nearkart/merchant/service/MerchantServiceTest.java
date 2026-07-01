package com.nearkart.merchant.service;

import com.nearkart.merchant.dto.MerchantRegistrationRequest;
import com.nearkart.merchant.dto.MerchantResponse;
import com.nearkart.merchant.entity.Merchant;
import com.nearkart.merchant.entity.MerchantStatus;
import com.nearkart.merchant.exception.MerchantAlreadyExistsException;
import com.nearkart.merchant.exception.ResourceNotFoundException;
import com.nearkart.merchant.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantService merchantService;

    private Merchant merchant;
    private UUID merchantId;

    @BeforeEach
    void setUp() {
        merchantId = UUID.randomUUID();
        merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setEmail("test@nearkart.com");
        merchant.setPhone("9999999999");
        merchant.setFullName("Test Merchant");
        merchant.setStatus(MerchantStatus.PENDING);
    }

    @Test
    void getMerchantById_shouldReturnMerchant_whenExists() {
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));

        MerchantResponse response = merchantService.getMerchantById(merchantId);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@nearkart.com");
        verify(merchantRepository, times(1)).findById(merchantId);
    }

    @Test
    void getMerchantById_shouldThrow_whenNotFound() {
        when(merchantRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> merchantService.getMerchantById(merchantId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registerMerchant_shouldThrow_whenEmailAlreadyExists() {
        MerchantRegistrationRequest request = new MerchantRegistrationRequest();
        request.setEmail("test@nearkart.com");
        request.setPhone("9999999999");

        when(merchantRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> merchantService.registerMerchant(request))
                .isInstanceOf(MerchantAlreadyExistsException.class);
    }

    @Test
    void registerMerchant_shouldSucceed_whenValidRequest() {
        MerchantRegistrationRequest request = new MerchantRegistrationRequest();
        request.setEmail("new@nearkart.com");
        request.setPhone("8888888888");
        request.setFullName("New Merchant");

        when(merchantRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(merchantRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);

        MerchantResponse response = merchantService.registerMerchant(request);

        assertThat(response).isNotNull();
        verify(merchantRepository).save(any(Merchant.class));
    }
}
