package com.nearkart.shopservice.controller;

import com.nearkart.shopservice.dto.ShopResponse;
import com.nearkart.shopservice.service.ShopService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShopController.class)
public class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShopService shopService;

    @Test
    public void testGetAllShops() throws Exception {
        Mockito.when(shopService.getAllShops()).thenReturn(List.of(new ShopResponse()));
        mockMvc.perform(get("/api/shops").contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }

    @Test
    public void testGetActiveShops() throws Exception {
        Mockito.when(shopService.getActiveShops()).thenReturn(List.of());
        mockMvc.perform(get("/api/shops/active"))
               .andExpect(status().isOk());
    }

    @Test
    public void testGetShopById_NotFound() throws Exception {
        Mockito.when(shopService.getShopById(99L))
               .thenThrow(new com.nearkart.shopservice.exception.ShopNotFoundException("Shop not found: 99"));
        mockMvc.perform(get("/api/shops/99"))
               .andExpect(status().isNotFound());
    }
}
