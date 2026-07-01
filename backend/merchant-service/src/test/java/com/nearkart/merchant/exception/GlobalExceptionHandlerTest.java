package com.nearkart.merchant.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundException_shouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Merchant not found");
        ResponseEntity<Map<String, String>> response = handler.handleResourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Merchant not found");
    }

    @Test
    void handleMerchantAlreadyExists_shouldReturn409() {
        MerchantAlreadyExistsException ex = new MerchantAlreadyExistsException("Email already registered");
        ResponseEntity<Map<String, String>> response = handler.handleMerchantAlreadyExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "Email already registered");
    }

    @Test
    void handleBusinessException_shouldReturn400() {
        BusinessException ex = new BusinessException("Invalid operation");
        ResponseEntity<Map<String, String>> response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
