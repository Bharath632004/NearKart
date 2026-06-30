package in.nearkart.notification.controller;

import in.nearkart.notification.dto.*;
import in.nearkart.notification.entity.NotificationLog;
import in.nearkart.notification.repository.NotificationLogRepository;
import in.nearkart.notification.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushService;
    private final DeviceTokenService deviceTokenService;
    private final NotificationLogRepository logRepository;

    // --- Email ---
    @PostMapping("/email")
    public ResponseEntity<NotificationResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(emailService.sendEmail(request));
    }

    // --- SMS ---
    @PostMapping("/sms")
    public ResponseEntity<NotificationResponse> sendSms(@Valid @RequestBody SmsRequest request) {
        return ResponseEntity.ok(smsService.sendSms(request));
    }

    // --- Push ---
    @PostMapping("/push")
    public ResponseEntity<NotificationResponse> sendPush(@Valid @RequestBody PushRequest request) {
        return ResponseEntity.ok(pushService.sendPush(request));
    }

    // --- Device Token Management ---
    @PostMapping("/device-token")
    public ResponseEntity<?> registerToken(@Valid @RequestBody DeviceTokenRequest request) {
        return ResponseEntity.ok(deviceTokenService.registerToken(request));
    }

    @DeleteMapping("/device-token/{token}")
    public ResponseEntity<Void> deregisterToken(@PathVariable String token) {
        deviceTokenService.deregisterToken(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/device-token/user/{userId}")
    public ResponseEntity<?> getTokens(@PathVariable String userId) {
        return ResponseEntity.ok(deviceTokenService.getTokensByUser(userId));
    }

    // --- Logs ---
    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<Page<NotificationLog>> getLogs(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(logRepository.findByUserId(
                userId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<NotificationLog> getLog(@PathVariable Long id) {
        return logRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Health / test ---
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("notification-service OK");
    }
}
