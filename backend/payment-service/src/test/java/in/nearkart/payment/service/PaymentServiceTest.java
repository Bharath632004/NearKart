package in.nearkart.payment.service;

import in.nearkart.payment.dto.request.VerifyPaymentRequest;
import in.nearkart.payment.entity.Payment;
import in.nearkart.payment.entity.PaymentMethod;
import in.nearkart.payment.entity.PaymentStatus;
import in.nearkart.payment.exception.PaymentNotFoundException;
import in.nearkart.payment.kafka.producer.PaymentEventProducer;
import in.nearkart.payment.repository.PaymentRepository;
import in.nearkart.payment.service.impl.PaymentServiceImpl;
import in.nearkart.payment.config.RazorpayConfig;
import com.razorpay.RazorpayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentEventProducer eventProducer;
    @Mock private RazorpayClient razorpayClient;
    @Mock private RazorpayConfig razorpayConfig;

    @InjectMocks private PaymentServiceImpl paymentService;

    @Test
    void getPaymentByOrderId_NotFound_Throws() {
        UUID orderId = UUID.randomUUID();
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.getPaymentByOrderId(orderId));
    }

    @Test
    void getPaymentByOrderId_Found_ReturnsResponse() {
        UUID orderId = UUID.randomUUID();
        Payment p = Payment.builder()
                .id(UUID.randomUUID()).orderId(orderId)
                .amount(new BigDecimal("235.00")).currency("INR")
                .status(PaymentStatus.SUCCESS).method(PaymentMethod.UPI)
                .razorpayOrderId("order_test123")
                .build();
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(p));
        var resp = paymentService.getPaymentByOrderId(orderId);
        assertNotNull(resp);
        assertEquals(PaymentStatus.SUCCESS, resp.getStatus());
    }
}
