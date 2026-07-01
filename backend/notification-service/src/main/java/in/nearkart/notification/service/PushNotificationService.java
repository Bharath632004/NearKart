package in.nearkart.notification.service;

import com.google.firebase.messaging.*;
import in.nearkart.notification.dto.NotificationResponse;
import in.nearkart.notification.dto.PushRequest;
import in.nearkart.notification.entity.NotificationChannel;
import in.nearkart.notification.entity.NotificationLog;
import in.nearkart.notification.entity.NotificationStatus;
import in.nearkart.notification.entity.NotificationType;
import in.nearkart.notification.repository.DeviceTokenRepository;
import in.nearkart.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationLogRepository logRepository;

    @Async
    public NotificationResponse sendPush(PushRequest request) {
        List<String> tokens = new ArrayList<>();

        if (request.getFcmToken() != null && !request.getFcmToken().isBlank()) {
            tokens.add(request.getFcmToken());
        } else if (request.getUserId() != null) {
            deviceTokenRepository.findByUserIdAndActiveTrue(request.getUserId())
                    .forEach(dt -> tokens.add(dt.getFcmToken()));
        }

        if (tokens.isEmpty()) {
            return NotificationResponse.builder().success(false).message("No device tokens found").build();
        }

        // Use fully-qualified FCM Notification to avoid clash with entity Notification
        com.google.firebase.messaging.Notification fcmNotification =
                com.google.firebase.messaging.Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .setImage(request.getImageUrl())
                        .build();

        List<Message> messages = tokens.stream().map(token -> {
            Message.Builder mb = Message.builder()
                    .setToken(token)
                    .setNotification(fcmNotification);
            if (request.getData() != null) mb.putAllData(request.getData());
            return mb.build();
        }).toList();

        try {
            BatchResponse response = firebaseMessaging.sendEach(messages);
            log.info("FCM batch: {}/{} success", response.getSuccessCount(), messages.size());

            for (int i = 0; i < response.getResponses().size(); i++) {
                SendResponse sr = response.getResponses().get(i);
                if (!sr.isSuccessful()) {
                    String code = sr.getException().getMessagingErrorCode() != null
                            ? sr.getException().getMessagingErrorCode().name() : "";
                    if (code.equals("UNREGISTERED") || code.equals("INVALID_ARGUMENT")) {
                        deviceTokenRepository.findByFcmToken(tokens.get(i)).ifPresent(dt -> {
                            dt.setActive(false);
                            deviceTokenRepository.save(dt);
                        });
                    }
                }
            }

            String userId = request.getUserId() != null ? request.getUserId() : "unknown";
            NotificationLog logEntry = NotificationLog.builder()
                    .userId(userId)
                    .channel(NotificationChannel.PUSH)
                    .type(resolveType(request.getNotificationType()))
                    .recipient(String.join(",", tokens))
                    .subject(request.getTitle())
                    .message(request.getBody())
                    .status(response.getSuccessCount() > 0 ? NotificationStatus.SENT : NotificationStatus.FAILED)
                    .deliveredAt(LocalDateTime.now())
                    .build();
            logRepository.save(logEntry);

            return NotificationResponse.builder()
                    .success(response.getSuccessCount() > 0)
                    .message(response.getSuccessCount() + " of " + messages.size() + " delivered")
                    .logId(logEntry.getId())
                    .build();
        } catch (FirebaseMessagingException e) {
            log.error("FCM error: {}", e.getMessage());
            return NotificationResponse.builder().success(false).message(e.getMessage()).build();
        }
    }

    private NotificationType resolveType(String type) {
        if (type == null) return NotificationType.GENERAL;
        try { return NotificationType.valueOf(type.toUpperCase()); }
        catch (Exception e) { return NotificationType.GENERAL; }
    }
}
