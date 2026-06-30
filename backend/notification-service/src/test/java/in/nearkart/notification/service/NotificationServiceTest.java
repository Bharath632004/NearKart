package in.nearkart.notification.service;

import in.nearkart.notification.dto.request.SendNotificationRequest;
import in.nearkart.notification.entity.*;
import in.nearkart.notification.repository.DeviceTokenRepository;
import in.nearkart.notification.repository.NotificationRepository;
import in.nearkart.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private DeviceTokenRepository  deviceTokenRepository;
    @Mock private FcmService   fcmService;
    @Mock private SmsService   smsService;
    @Mock private EmailService emailService;

    @InjectMocks private NotificationServiceImpl notificationService;

    @Test
    void send_Push_CallsFcmService() {
        UUID userId = UUID.randomUUID();
        Notification saved = Notification.builder()
                .id(UUID.randomUUID()).userId(userId)
                .type(NotificationType.ORDER_PLACED)
                .channel(NotificationChannel.PUSH)
                .title("t").body("b")
                .deliveryStatus(DeliveryStatus.PENDING)
                .isRead(false)
                .build();

        when(notificationRepository.save(any())).thenReturn(saved);
        doNothing().when(fcmService).sendToUser(any(), any(), any(), any());

        SendNotificationRequest req = SendNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.ORDER_PLACED)
                .channel(NotificationChannel.PUSH)
                .templateArgs(new String[]{"NK-001", "235.00"})
                .build();

        notificationService.send(req);

        verify(notificationRepository, times(2)).save(any());
        verify(fcmService, times(1)).sendToUser(any(), any(), any(), any());
    }

    @Test
    void getUnreadCount_ReturnsCorrectCount() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(7L);
        var resp = notificationService.getUnreadCount(userId);
        assert resp.getUnreadCount() == 7L;
    }
}
