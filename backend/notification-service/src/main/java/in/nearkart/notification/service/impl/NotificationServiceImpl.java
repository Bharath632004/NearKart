package in.nearkart.notification.service.impl;

import in.nearkart.notification.dto.request.DeviceTokenRequest;
import in.nearkart.notification.dto.request.SendNotificationRequest;
import in.nearkart.notification.dto.response.NotificationResponse;
import in.nearkart.notification.dto.response.UnreadCountResponse;
import in.nearkart.notification.entity.*;
import in.nearkart.notification.repository.DeviceTokenRepository;
import in.nearkart.notification.repository.NotificationRepository;
import in.nearkart.notification.service.EmailService;
import in.nearkart.notification.service.FcmService;
import in.nearkart.notification.service.NotificationService;
import in.nearkart.notification.service.SmsService;
import in.nearkart.notification.template.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository  deviceTokenRepository;
    private final FcmService   fcmService;
    private final SmsService   smsService;
    private final EmailService emailService;

    @Override
    public void send(SendNotificationRequest req) {
        NotificationTemplate tpl = NotificationTemplate.of(
                req.getType(), req.getTemplateArgs());

        // Persist notification record
        in.nearkart.notification.entity.Notification notification =
                in.nearkart.notification.entity.Notification.builder()
                        .userId(req.getUserId())
                        .type(req.getType())
                        .channel(req.getChannel())
                        .title(tpl.getTitle())
                        .body(tpl.getBody())
                        .referenceId(req.getReferenceId())
                        .referenceType(req.getReferenceType())
                        .deliveryStatus(DeliveryStatus.PENDING)
                        .build();

        in.nearkart.notification.entity.Notification saved = notificationRepository.save(notification);

        try {
            switch (req.getChannel()) {
                case PUSH  -> fcmService.sendToUser(
                        req.getUserId(), tpl.getTitle(), tpl.getBody(),
                        req.getReferenceId() != null ? req.getReferenceId().toString() : null);
                case SMS   -> smsService.sendSms(
                        req.getPhoneNumber(), tpl.getBody());
                case EMAIL -> emailService.sendPlainEmail(
                        req.getEmail(), tpl.getTitle(), tpl.getBody());
            }

            saved.setDeliveryStatus(DeliveryStatus.SENT);
            saved.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            saved.setDeliveryStatus(DeliveryStatus.FAILED);
            saved.setFailureReason(e.getMessage());
            log.error("Notification dispatch failed: type={}, userId={}: {}",
                    req.getType(), req.getUserId(), e.getMessage());
        }

        notificationRepository.save(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new UnreadCountResponse(userId, count);
    }

    @Override
    public void markAllRead(UUID userId) {
        int updated = notificationRepository.markAllReadByUserId(userId);
        log.debug("Marked {} notifications read for userId={}", updated, userId);
    }

    @Override
    public void markOneRead(UUID notificationId) {
        notificationRepository.markOneRead(notificationId);
    }

    @Override
    public void registerDeviceToken(UUID userId, DeviceTokenRequest request) {
        // Soft-deactivate old entries for same token
        deviceTokenRepository.deactivateByToken(request.getToken());

        DeviceToken deviceToken = DeviceToken.builder()
                .userId(userId.toString())   // DeviceToken.userId is String
                .fcmToken(request.getToken())
                .platform(request.getPlatform())
                .active(true)
                .build();

        deviceTokenRepository.save(deviceToken);
        log.info("Device token registered: userId={}, platform={}", userId, request.getPlatform());
    }

    @Override
    public void removeDeviceToken(String token) {
        deviceTokenRepository.deactivateByToken(token);
    }

    private NotificationResponse toResponse(in.nearkart.notification.entity.Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).userId(n.getUserId())
                .type(n.getType()).channel(n.getChannel())
                .title(n.getTitle()).body(n.getBody())
                .referenceId(n.getReferenceId()).referenceType(n.getReferenceType())
                .isRead(n.getIsRead()).deliveryStatus(n.getDeliveryStatus())
                .createdAt(n.getCreatedAt()).readAt(n.getReadAt())
                .build();
    }
}
