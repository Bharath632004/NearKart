package in.nearkart.notification.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import in.nearkart.notification.config.TwilioConfig;
import in.nearkart.notification.dto.NotificationResponse;
import in.nearkart.notification.dto.SmsRequest;
import in.nearkart.notification.entity.*;
import in.nearkart.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final TwilioConfig twilioConfig;
    private final NotificationLogRepository logRepository;

    @Async
    public NotificationResponse sendSms(SmsRequest request) {
        NotificationLog logEntry = NotificationLog.builder()
                .userId(request.getUserId())
                .channel(NotificationChannel.SMS)
                .type(resolveType(request.getNotificationType()))
                .recipient(request.getTo())
                .message(request.getMessage())
                .status(NotificationStatus.PENDING)
                .build();
        logRepository.save(logEntry);

        try {
            Message message = Message.creator(
                    new PhoneNumber(request.getTo()),
                    new PhoneNumber(twilioConfig.getFromNumber()),
                    request.getMessage()
            ).create();

            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setExternalId(message.getSid());
            logEntry.setDeliveredAt(LocalDateTime.now());
            logRepository.save(logEntry);

            log.info("SMS sent to {} sid={}", request.getTo(), message.getSid());
            return NotificationResponse.builder()
                    .success(true)
                    .message("SMS sent")
                    .externalId(message.getSid())
                    .logId(logEntry.getId())
                    .build();
        } catch (Exception e) {
            log.error("SMS failed to {}: {}", request.getTo(), e.getMessage());
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorMessage(e.getMessage());
            logRepository.save(logEntry);
            return NotificationResponse.builder().success(false).message(e.getMessage()).logId(logEntry.getId()).build();
        }
    }

    /**
     * Convenience overload used by NotificationServiceImpl.
     * Wraps the two-arg call into a SmsRequest.
     */
    public NotificationResponse sendSms(String toPhone, String messageBody) {
        SmsRequest req = new SmsRequest();
        req.setTo(toPhone);
        req.setMessage(messageBody);
        return sendSms(req);
    }

    private NotificationType resolveType(String type) {
        if (type == null) return NotificationType.GENERAL;
        try { return NotificationType.valueOf(type.toUpperCase()); }
        catch (Exception e) { return NotificationType.GENERAL; }
    }
}
