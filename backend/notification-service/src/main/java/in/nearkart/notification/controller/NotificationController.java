package in.nearkart.notification.controller;

import in.nearkart.notification.dto.request.DeviceTokenRequest;
import in.nearkart.notification.dto.response.ApiResponse;
import in.nearkart.notification.dto.response.NotificationResponse;
import in.nearkart.notification.dto.response.UnreadCountResponse;
import in.nearkart.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Get paginated notification inbox for a user */
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MERCHANT', 'DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched",
                notificationService.getUserNotifications(userId, PageRequest.of(page, size))));
    }

    /** Get unread notification badge count */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Unread count",
                notificationService.getUnreadCount(userId)));
    }

    /** Mark all notifications as read */
    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @RequestHeader("X-User-Id") UUID userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }

    /** Mark a single notification as read */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markOneRead(@PathVariable UUID id) {
        notificationService.markOneRead(id);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    /** Register / refresh FCM device token */
    @PostMapping("/device-token")
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody DeviceTokenRequest request) {
        notificationService.registerDeviceToken(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Device token registered", null));
    }

    /** Remove / logout device token */
    @DeleteMapping("/device-token")
    public ResponseEntity<ApiResponse<Void>> removeToken(
            @RequestParam String token) {
        notificationService.removeDeviceToken(token);
        return ResponseEntity.ok(ApiResponse.success("Device token removed", null));
    }
}
