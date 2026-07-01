package com.nearkart.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearkart.merchant.dto.MerchantRegistrationRequest;
import com.nearkart.merchant.dto.MerchantResponse;
import com.nearkart.merchant.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MerchantController.class)
class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MerchantService merchantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "MERCHANT")
    void registerMerchant_shouldReturn201_whenValidRequest() throws Exception {
        MerchantRegistrationRequest request = new MerchantRegistrationRequest();
        request.setEmail("test@nearkart.com");
        request.setPhone("9999999999");
        request.setFullName("Test Merchant");

        MerchantResponse response = new MerchantResponse();
        response.setId(UUID.randomUUID());
        response.setEmail("test@nearkart.com");

        when(merchantService.registerMerchant(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/merchants/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@nearkart.com"));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void getMerchantById_shouldReturn200_whenExists() throws Exception {
        UUID id = UUID.randomUUID();
        MerchantResponse response = new MerchantResponse();
        response.setId(id);
        response.setEmail("test@nearkart.com");

        when(merchantService.getMerchantById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/merchants/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }
}
