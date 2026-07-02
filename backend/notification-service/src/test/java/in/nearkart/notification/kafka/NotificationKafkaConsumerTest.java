package in.nearkart.notification.kafka;

import in.nearkart.notification.dto.*;
import in.nearkart.notification.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaConsumerTest {

    @Mock private EmailService emailService;
    @Mock private SmsService smsService;
    @Mock private PushNotificationService pushService;

    @InjectMocks
    private NotificationKafkaConsumer consumer;

    // ── Test 1: order.placed triggers email + sms + push ────────────────────────
    @Test
    void onOrderPlaced_triggersAllChannels() {
        Map<String, Object> event = Map.of(
                "userId", "u1",
                "orderId", "ORD-001",
                "customerEmail", "user@example.com",
                "customerPhone", "+919876543210"
        );
        when(emailService.sendEmail(any())).thenReturn(NotificationResponse.builder().success(true).build());
        when(smsService.sendSms(any(SmsRequest.class))).thenReturn(NotificationResponse.builder().success(true).build());
        when(pushService.sendPush(any())).thenReturn(NotificationResponse.builder().success(true).build());

        consumer.onOrderPlaced(event);

        verify(emailService).sendEmail(any());
        verify(smsService).sendSms(any(SmsRequest.class));
        verify(pushService).sendPush(any());
    }

    // ── Test 2: order.placed with no email/phone still sends push ───────────────
    @Test
    void onOrderPlaced_noEmailOrPhone_onlyPush() {
        Map<String, Object> event = Map.of("userId", "u2", "orderId", "ORD-002");
        when(pushService.sendPush(any())).thenReturn(NotificationResponse.builder().success(true).build());

        consumer.onOrderPlaced(event);

        verify(emailService, never()).sendEmail(any());
        verify(smsService, never()).sendSms(any(SmsRequest.class));
        verify(pushService).sendPush(any());
    }

    // ── Test 3: order.shipped triggers sms + push ───────────────────────────
    @Test
    void onOrderShipped_triggersSmsAndPush() {
        Map<String, Object> event = Map.of(
                "userId", "u3",
                "orderId", "ORD-003",
                "customerPhone", "+919876543210"
        );
        when(smsService.sendSms(any(SmsRequest.class))).thenReturn(NotificationResponse.builder().success(true).build());
        when(pushService.sendPush(any())).thenReturn(NotificationResponse.builder().success(true).build());

        consumer.onOrderShipped(event);

        verify(smsService).sendSms(any(SmsRequest.class));
        verify(pushService).sendPush(any());
        verify(emailService, never()).sendEmail(any());
    }

    // ── Test 4: OTP triggers sms only ───────────────────────────────────────
    @Test
    void onOtp_triggersSmsOnly() {
        Map<String, Object> event = Map.of(
                "userId", "u4",
                "phone", "+919876543210",
                "otp", "123456"
        );
        when(smsService.sendSms(any(SmsRequest.class))).thenReturn(NotificationResponse.builder().success(true).build());

        consumer.onOtp(event);

        verify(smsService).sendSms(any(SmsRequest.class));
        verify(emailService, never()).sendEmail(any());
        verify(pushService, never()).sendPush(any());
    }

    // ── Test 5: channel failure does NOT propagate to other channels ─────────
    @Test
    void onOrderPlaced_emailFailure_doesNotBlockSmsOrPush() {
        Map<String, Object> event = Map.of(
                "userId", "u5",
                "orderId", "ORD-005",
                "customerEmail", "bad@example.com",
                "customerPhone", "+919876543210"
        );
        when(emailService.sendEmail(any())).thenThrow(new RuntimeException("SMTP error"));
        when(smsService.sendSms(any(SmsRequest.class))).thenReturn(NotificationResponse.builder().success(true).build());
        when(pushService.sendPush(any())).thenReturn(NotificationResponse.builder().success(true).build());

        // Should NOT throw
        consumer.onOrderPlaced(event);

        verify(smsService).sendSms(any(SmsRequest.class));
        verify(pushService).sendPush(any());
    }
}
