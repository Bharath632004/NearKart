package in.nearkart.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stored as String (legacy), but we also keep a UUID-typed transient for service layer
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 500)
    private String fcmToken;

    // Alias field 'token' used by FcmService and PushNotificationService
    @Transient
    public String getToken() { return fcmToken; }

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "platform", length = 20)
    private String platform;

    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    // Boolean alias used by NotificationServiceImpl builder (.isActive(true))
    @Transient
    public Boolean getIsActive() { return active; }

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Setter used by NotificationServiceImpl
    public void setIsActive(Boolean isActive) {
        this.active = Boolean.TRUE.equals(isActive);
    }

    // Helper to set userId from UUID
    public void setUserId(UUID uuid) {
        this.userId = uuid != null ? uuid.toString() : null;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
