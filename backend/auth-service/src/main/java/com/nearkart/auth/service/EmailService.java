package com.nearkart.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendPasswordResetOtp(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("NearKart - Password Reset OTP");
            message.setText(
                "Your OTP for password reset is: " + otp +
                "\n\nThis OTP is valid for 15 minutes." +
                "\n\nIf you did not request this, please ignore this email." +
                "\n\nTeam NearKart"
            );
            mailSender.send(message);
            log.info("Password reset OTP sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Welcome to NearKart!");
            message.setText(
                "Hi " + name + ",\n\n" +
                "Welcome to NearKart \u2014 your hyperlocal quick-commerce platform!\n\n" +
                "Start shopping now for groceries and essentials delivered in minutes.\n\n" +
                "Team NearKart"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }
}
