package in.nearkart.notification.service;

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

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender    mailSender;
    private final TemplateEngine   templateEngine;

    @Value("${spring.mail.from:noreply@nearkart.in}")
    private String fromEmail;

    @Async("notificationExecutor")
    public void sendHtmlEmail(String toEmail, String subject,
                               String templateName, Map<String, Object> variables) {
        try {
            Context ctx = new Context();
            if (variables != null) variables.forEach(ctx::setVariable);

            String htmlContent = templateEngine.process(templateName, ctx);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email sent: to={}, subject={}", toEmail, subject);
        } catch (Exception e) {
            log.error("Email send failed to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("notificationExecutor")
    public void sendPlainEmail(String toEmail, String subject, String text) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(mimeMessage);
            log.info("Plain email sent: to={}", toEmail);
        } catch (Exception e) {
            log.error("Plain email failed to {}: {}", toEmail, e.getMessage());
        }
    }
}
