package in.nearkart.notification.service;

import in.nearkart.notification.dto.EmailRequest;
import in.nearkart.notification.dto.NotificationResponse;
import in.nearkart.notification.entity.*;
import in.nearkart.notification.repository.NotificationLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationLogRepository logRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name:NearKart}")
    private String fromName;

    @Async
    public NotificationResponse sendEmail(EmailRequest request) {
        NotificationLog logEntry = NotificationLog.builder()
                .userId(request.getUserId())
                .channel(NotificationChannel.EMAIL)
                .type(resolveType(request.getNotificationType()))
                .recipient(request.getTo())
                .subject(request.getSubject())
                .status(NotificationStatus.PENDING)
                .build();
        logRepository.save(logEntry);

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());

            String html;
            if (request.getTemplateName() != null && !request.getTemplateName().isBlank()) {
                Context ctx = new Context();
                if (request.getTemplateVariables() != null)
                    request.getTemplateVariables().forEach(ctx::setVariable);
                html = templateEngine.process(request.getTemplateName(), ctx);
            } else {
                html = request.getBody();
            }
            helper.setText(html, true);
            mailSender.send(mime);

            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setDeliveredAt(LocalDateTime.now());
            logEntry.setMessage(html);
            logRepository.save(logEntry);

            log.info("Email sent to {} subject={}", request.getTo(), request.getSubject());
            return NotificationResponse.builder().success(true).message("Email sent").logId(logEntry.getId()).build();
        } catch (Exception e) {
            log.error("Email failed to {}: {}", request.getTo(), e.getMessage());
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorMessage(e.getMessage());
            logRepository.save(logEntry);
            return NotificationResponse.builder().success(false).message(e.getMessage()).logId(logEntry.getId()).build();
        }
    }

    private NotificationType resolveType(String type) {
        if (type == null) return NotificationType.GENERAL;
        try { return NotificationType.valueOf(type.toUpperCase()); }
        catch (Exception e) { return NotificationType.GENERAL; }
    }
}
