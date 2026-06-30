package in.nearkart.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UnreadCountResponse {
    private UUID userId;
    private long unreadCount;
}
