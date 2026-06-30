package in.nearkart.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private boolean success;
    private String message;
    private String externalId;
    private Long logId;
}
