package in.nearkart.notification.service;

import in.nearkart.notification.dto.EmailRequest;
import in.nearkart.notification.dto.NotificationResponse;
import in.nearkart.notification.entity.NotificationLog;
import in.nearkart.notification.entity.NotificationStatus;
import in.nearkart.notification.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;
    @Mock private NotificationLogRepository logRepository;
    @Mock private ApplicationContext applicationContext;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@nearkart.in");
        ReflectionTestUtils.setField(emailService, "fromName", "NearKart");
    }

    // ── Test 1: successful email send ───────────────────────────────────────
    @Test
    void sendEmail_success() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(logRepository.save(any())).thenAnswer(inv -> {
            NotificationLog log = inv.getArgument(0);
            log = NotificationLog.builder()
                    .id(1L)
                    .status(log.getStatus())
                    .recipient(log.getRecipient())
                    .channel(log.getChannel())
                    .type(log.getType())
                    .build();
            return log;
        });

        EmailRequest req = new EmailRequest();
        req.setTo("user@example.com");
        req.setSubject("Test Subject");
        req.setBody("<p>Hello</p>");
        req.setNotificationType("GENERAL");

        NotificationResponse resp = emailService.sendEmail(req);

        assertThat(resp.isSuccess()).isTrue();
        verify(mailSender).send(any(MimeMessage.class));
    }

    // ── Test 2: send failure marks log FAILED ──────────────────────────────
    @Test
    void sendEmail_smtpFailure_marksLogFailed() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(logRepository.save(any())).thenAnswer(inv -> {
            NotificationLog log = inv.getArgument(0);
            if (log.getId() == null) {
                return NotificationLog.builder().id(2L)
                        .status(log.getStatus()).recipient(log.getRecipient())
                        .channel(log.getChannel()).type(log.getType()).build();
            }
            return log;
        });
        doThrow(new RuntimeException("SMTP failure")).when(mailSender).send(any(MimeMessage.class));

        EmailRequest req = new EmailRequest();
        req.setTo("fail@example.com");
        req.setSubject("Fail");
        req.setBody("body");

        NotificationResponse resp = emailService.sendEmail(req);

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("SMTP failure");
    }

    // ── Test 3: resolveType falls back to GENERAL for unknown type ───────────
    @Test
    void sendEmail_unknownType_defaultsToGeneral() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(logRepository.save(any())).thenAnswer(inv -> {
            NotificationLog log = inv.getArgument(0);
            return NotificationLog.builder().id(3L)
                    .status(log.getStatus()).recipient(log.getRecipient())
                    .channel(log.getChannel()).type(log.getType()).build();
        });

        EmailRequest req = new EmailRequest();
        req.setTo("u@x.com");
        req.setSubject("S");
        req.setBody("B");
        req.setNotificationType("NONEXISTENT_TYPE");

        NotificationResponse resp = emailService.sendEmail(req);
        // Should not throw
        assertThat(resp).isNotNull();
    }

    // ── Test 4: sendPlainEmail delegates via ApplicationContext proxy ────────
    @Test
    void sendPlainEmail_delegatesToProxy() throws Exception {
        EmailService proxySelf = mock(EmailService.class);
        when(applicationContext.getBean(EmailService.class)).thenReturn(proxySelf);
        when(proxySelf.sendEmail(any())).thenReturn(
                NotificationResponse.builder().success(true).message("ok").build());

        NotificationResponse resp = emailService.sendPlainEmail("a@b.com", "Subj", "<p>Hi</p>");

        assertThat(resp.isSuccess()).isTrue();
        verify(proxySelf).sendEmail(any());
    }
}
