package in.nearkart.notification.kafka;

import in.nearkart.notification.dto.*;
import in.nearkart.notification.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listens to Kafka events and fans out to Email / SMS / Push channels.
 *
 * Error handling strategy:
 *  - Each handler is wrapped in try-catch so one channel failure does
 *    not prevent other channels from being notified.
 *  - If a handler throws uncaught, the KafkaConsumerConfig DefaultErrorHandler
 *    will retry 3x then publish to the .DLT topic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushService;

    // ---- Order events ----------------------------------------------------------------

    @KafkaListener(topics = "order.placed", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void onOrderPlaced(Map<String, Object> event) {
        log.info("Kafka [order.placed]: orderId={}", event.get("orderId"));
        String email   = str(event, "customerEmail");
        String phone   = str(event, "customerPhone");
        String userId  = str(event, "userId");
        String orderId = str(event, "orderId");

        if (email != null) {
            try {
                EmailRequest req = buildEmailReq(userId, email,
                        "Order Placed \u2013 #" + orderId,
                        "order-placed",
                        Map.of("orderId", orderId, "event", event),
                        "ORDER_PLACED");
                emailService.sendEmail(req);
            } catch (Exception e) {
                log.error("[order.placed] Email failed for orderId={}: {}", orderId, e.getMessage());
            }
        }
        if (phone != null) {
            try {
                SmsRequest req = buildSmsReq(userId, phone,
                        "NearKart: Your order #" + orderId + " has been placed successfully!",
                        "ORDER_PLACED");
                smsService.sendSms(req);
            } catch (Exception e) {
                log.error("[order.placed] SMS failed for orderId={}: {}", orderId, e.getMessage());
            }
        }
        if (userId != null) {
            try {
                PushRequest req = buildPushReq(userId,
                        "Order Placed \uD83C\uDF89",
                        "Order #" + orderId + " placed. We\u2019ll notify you when it ships.",
                        "ORDER_PLACED");
                pushService.sendPush(req);
            } catch (Exception e) {
                log.error("[order.placed] Push failed for orderId={}: {}", orderId, e.getMessage());
            }
        }
    }

    @KafkaListener(topics = "order.shipped", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void onOrderShipped(Map<String, Object> event) {
        log.info("Kafka [order.shipped]: orderId={}", event.get("orderId"));
        String userId  = str(event, "userId");
        String orderId = str(event, "orderId");
        String phone   = str(event, "customerPhone");

        if (phone != null) {
            try {
                smsService.sendSms(buildSmsReq(userId, phone,
                        "NearKart: Order #" + orderId + " has been shipped!", "ORDER_SHIPPED"));
            } catch (Exception e) {
                log.error("[order.shipped] SMS failed for orderId={}: {}", orderId, e.getMessage());
            }
        }
        if (userId != null) {
            try {
                pushService.sendPush(buildPushReq(userId,
                        "Order Shipped \uD83D\uDE9A",
                        "Order #" + orderId + " is on its way!", "ORDER_SHIPPED"));
            } catch (Exception e) {
                log.error("[order.shipped] Push failed for orderId={}: {}", orderId, e.getMessage());
            }
        }
    }

    @KafkaListener(topics = "order.delivered", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void onOrderDelivered(Map<String, Object> event) {
        log.info("Kafka [order.delivered]: orderId={}", event.get("orderId"));
        String userId  = str(event, "userId");
        String orderId = str(event, "orderId");
        if (userId != null) {
            try {
                pushService.sendPush(buildPushReq(userId,
                        "Order Delivered \u2705",
                        "Order #" + orderId + " delivered. Enjoy your purchase!",
                        "ORDER_DELIVERED"));
            } catch (Exception e) {
                log.error("[order.delivered] Push failed for orderId={}: {}", orderId, e.getMessage());
            }
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void onOrderCancelled(Map<String, Object> event) {
        log.info("Kafka [order.cancelled]: orderId={}", event.get("orderId"));
        String userId  = str(event, "userId");
        String email   = str(event, "customerEmail");
        String orderId = str(event, "orderId");
        if (email != null) {
            try {
                EmailRequest req = buildEmailReq(userId, email,
                        "Order Cancelled \u2013 #" + orderId, null, null, "ORDER_CANCELLED");
                req.setBody("<p>Your order <b>#" + orderId + "</b> has been cancelled.</p>");
                emailService.sendEmail(req);
            } catch (Exception e) {
                log.error("[order.cancelled] Email failed for orderId={}: {}", orderId, e.getMessage());
            }
        }
    }

    // ---- Payment events --------------------------------------------------------------

    @KafkaListener(topics = "payment.success", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void onPaymentSuccess(Map<String, Object> event) {
        log.info("Kafka [payment.success]");
        String userId = str(event, "userId");
        String email  = str(event, "customerEmail");
        String amount = str(event, "amount");
        if (email != null) {
            try {
                EmailRequest req = buildEmailReq(userId, email,
                        "Payment Successful \u2013 \u20B9" + amount, null, null, "PAYMENT_SUCCESS");
                req.setBody("<p>Payment of <b>\u20B9" + amount + "</b> received. Thank you!</p>");
                emailService.sendEmail(req);
            } catch (Exception e) {
                log.error("[payment.success] Email failed: {}", e.getMessage());
            }
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void onPaymentFailed(Map<String, Object> event) {
        log.info("Kafka [payment.failed]");
        String userId = str(event, "userId");
        String email  = str(event, "customerEmail");
        if (email != null) {
            try {
                EmailRequest req = buildEmailReq(userId, email,
                        "Payment Failed \u2013 Action Required", null, null, "PAYMENT_FAILED");
                req.setBody("<p>Your payment could not be processed. Please retry.</p>");
                emailService.sendEmail(req);
            } catch (Exception e) {
                log.error("[payment.failed] Email failed: {}", e.getMessage());
            }
        }
    }

    // ---- OTP event -------------------------------------------------------------------

    @KafkaListener(topics = "notification.otp", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void onOtp(Map<String, Object> event) {
        log.info("Kafka [notification.otp]");
        String phone  = str(event, "phone");
        String otp    = str(event, "otp");
        String userId = str(event, "userId");
        if (phone != null && otp != null) {
            try {
                smsService.sendSms(buildSmsReq(userId, phone,
                        "NearKart OTP: " + otp + ". Valid for 5 minutes. Do not share.",
                        "OTP"));
            } catch (Exception e) {
                log.error("[notification.otp] SMS failed: {}", e.getMessage());
            }
        }
    }

    // ---- Private builders ------------------------------------------------------------

    private EmailRequest buildEmailReq(String userId, String to, String subject,
                                       String templateName, Map<String, Object> templateVars,
                                       String type) {
        EmailRequest req = new EmailRequest();
        req.setUserId(userId);
        req.setTo(to);
        req.setSubject(subject);
        req.setTemplateName(templateName);
        req.setTemplateVariables(templateVars);
        req.setNotificationType(type);
        return req;
    }

    private SmsRequest buildSmsReq(String userId, String to, String message, String type) {
        SmsRequest req = new SmsRequest();
        req.setUserId(userId);
        req.setTo(to);
        req.setMessage(message);
        req.setNotificationType(type);
        return req;
    }

    private PushRequest buildPushReq(String userId, String title, String body, String type) {
        PushRequest req = new PushRequest();
        req.setUserId(userId);
        req.setTitle(title);
        req.setBody(body);
        req.setNotificationType(type);
        return req;
    }

    private String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
