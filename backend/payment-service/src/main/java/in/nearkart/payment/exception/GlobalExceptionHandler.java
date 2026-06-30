package in.nearkart.payment.exception;

import in.nearkart.payment.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "PAYMENT_NOT_FOUND"));
    }

    @ExceptionHandler(SignatureVerificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleSignature(SignatureVerificationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "SIGNATURE_INVALID"));
    }

    @ExceptionHandler(InsufficientWalletBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientBalance(InsufficientWalletBalanceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "INSUFFICIENT_WALLET_BALANCE"));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleWalletNotFound(WalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "WALLET_NOT_FOUND"));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentException(PaymentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(ex.getMessage(), "PAYMENT_GATEWAY_ERROR"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e ->
                errors.put(((FieldError) e).getField(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false).message("Validation failed")
                        .data(errors).errorCode("VALIDATION_ERROR").build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_SERVER_ERROR"));
    }
}
