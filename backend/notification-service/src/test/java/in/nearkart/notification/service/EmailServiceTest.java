package in.nearkart.notification.service;

import in.nearkart.notification.dto.EmailRequest;
import in.nearkart.notification.dto.NotificationResponse;
import in.nearkart.notification.entity.NotificationStatus;
import in.nearkart.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock TemplateEngine templateEngine;
    @Mock NotificationLogRepository logRepository;

    @InjectMocks EmailService emailService;

    @BeforeEach
    void setup() { MockitoAnnotations.openMocks(this); }

    @Test
    void sendEmail_shouldReturnSuccess() {
        var mimeMsg = mock(jakarta.mail.internet.MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMsg);
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmailRequest req = new EmailRequest();
        req.setTo("test@nearkart.in");
        req.setSubject("Test");
        req.setBody("<p>Hello</p>");
        req.setNotificationType("GENERAL");

        NotificationResponse response = emailService.sendEmail(req);
        assertThat(response.isSuccess()).isTrue();
    }
}
