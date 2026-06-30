package in.nearkart.notification.service;

import in.nearkart.notification.dto.request.DeviceTokenRequest;
import in.nearkart.notification.dto.request.SendNotificationRequest;
import in.nearkart.notification.dto.response.NotificationResponse;
import in.nearkart.notification.dto.response.UnreadCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    void send(SendNotificationRequest request);
    Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable);
    UnreadCountResponse getUnreadCount(UUID userId);
    void markAllRead(UUID userId);
    void markOneRead(UUID notificationId);
    void registerDeviceToken(UUID userId, DeviceTokenRequest request);
    void removeDeviceToken(String token);
}
