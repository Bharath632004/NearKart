package in.nearkart.notification.service;

import com.google.firebase.messaging.*;
import in.nearkart.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Send push notification to ALL active devices of a user.
     */
    @Async("notificationExecutor")
    public void sendToUser(UUID userId, String title, String body, String referenceId) {
        List<String> tokens = deviceTokenRepository
                .findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(dt -> dt.getToken())
                .toList();

        if (tokens.isEmpty()) {
            log.debug("No active FCM tokens for userId={}", userId);
            return;
        }

        for (String token : tokens) {
            sendToToken(token, title, body, referenceId, userId);
        }
    }

    /**
     * Send to a specific FCM registration token.
     */
    private void sendToToken(String token, String title, String body,
                              String referenceId, UUID userId) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("referenceId", referenceId != null ? referenceId : "")
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setSound("default")
                                .setChannelId("nearkart_orders")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .setBadge(1)
                                .build())
                        .build())
                .build();

        try {
            String messageId = firebaseMessaging.send(message);
            log.info("FCM sent: userId={}, messageId={}", userId, messageId);
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed for token={}: {}", token, e.getMessage());
            // Deactivate invalid/expired tokens automatically
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                deviceTokenRepository.deactivateByToken(token);
                log.info("Deactivated invalid FCM token for userId={}", userId);
            }
        }
    }

    /**
     * Send to a Kafka topic-based multicast (e.g. promotional blast).
     */
    @Async("notificationExecutor")
    public void sendMulticast(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) return;

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title).setBody(body).build())
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info("FCM multicast: success={}, failure={}",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast failed: {}", e.getMessage());
        }
    }
}
