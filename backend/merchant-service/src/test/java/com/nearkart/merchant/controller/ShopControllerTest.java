package com.nearkart.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearkart.merchant.dto.ShopRequest;
import com.nearkart.merchant.dto.ShopResponse;
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

@WebMvcTest(ShopController.class)
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MerchantService merchantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "MERCHANT")
    void createShop_shouldReturn201() throws Exception {
        ShopRequest request = new ShopRequest();
        request.setShopName("Test Shop");
        request.setAddress("123 Main St");

        ShopResponse response = new ShopResponse();
        response.setId(UUID.randomUUID());
        response.setShopName("Test Shop");

        when(merchantService.createShop(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/shops")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
