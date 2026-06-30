package in.nearkart.notification.dto.request;

import in.nearkart.notification.entity.NotificationChannel;
import in.nearkart.notification.entity.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SendNotificationRequest {
    private UUID userId;
    private NotificationType type;
    private NotificationChannel channel;
    private UUID referenceId;
    private String referenceType;
    private String[] templateArgs;
    private String phoneNumber;   // for SMS
    private String email;         // for EMAIL
}
