package in.nearkart.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nearkart.notification.dto.*;
import in.nearkart.notification.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushService;
    private final ObjectMapper objectMapper;

    // ---- Order events ----
    @KafkaListener(topics = "order.placed", groupId = "notification-service")
    public void onOrderPlaced(Map<String, Object> event) {
        log.info("Kafka [order.placed]: {}", event);
        String email = getString(event, "customerEmail");
        String phone = getString(event, "customerPhone");
        String userId = getString(event, "userId");
        String orderId = getString(event, "orderId");

        if (email != null) {
            EmailRequest req = new EmailRequest();
            req.setUserId(userId);
            req.setTo(email);
            req.setSubject("Order Placed – " + orderId);
            req.setTemplateName("order-placed");
            req.setTemplateVariables(Map.of("orderId", orderId, "event", event));
            req.setNotificationType("ORDER_PLACED");
            emailService.sendEmail(req);
        }
        if (phone != null) {
            SmsRequest req = new SmsRequest();
            req.setUserId(userId);
            req.setTo(phone);
            req.setMessage("NearKart: Your order #" + orderId + " has been placed successfully!");
            req.setNotificationType("ORDER_PLACED");
            smsService.sendSms(req);
        }
        if (userId != null) {
            PushRequest req = new PushRequest();
            req.setUserId(userId);
            req.setTitle("Order Placed 🎉");
            req.setBody("Order #" + orderId + " placed. We will notify you when it ships.");
            req.setNotificationType("ORDER_PLACED");
            pushService.sendPush(req);
        }
    }

    @KafkaListener(topics = "order.shipped", groupId = "notification-service")
    public void onOrderShipped(Map<String, Object> event) {
        log.info("Kafka [order.shipped]: {}", event);
        String userId = getString(event, "userId");
        String orderId = getString(event, "orderId");
        String phone  = getString(event, "customerPhone");

        if (phone != null) {
            SmsRequest req = new SmsRequest();
            req.setUserId(userId);
            req.setTo(phone);
            req.setMessage("NearKart: Order #" + orderId + " has been shipped!");
            req.setNotificationType("ORDER_SHIPPED");
            smsService.sendSms(req);
        }
        if (userId != null) {
            PushRequest req = new PushRequest();
            req.setUserId(userId);
            req.setTitle("Order Shipped 🚚");
            req.setBody("Order #" + orderId + " is on its way!");
            req.setNotificationType("ORDER_SHIPPED");
            pushService.sendPush(req);
        }
    }

    @KafkaListener(topics = "order.delivered", groupId = "notification-service")
    public void onOrderDelivered(Map<String, Object> event) {
        String userId = getString(event, "userId");
        String orderId = getString(event, "orderId");
        if (userId != null) {
            PushRequest req = new PushRequest();
            req.setUserId(userId);
            req.setTitle("Order Delivered ✅");
            req.setBody("Order #" + orderId + " delivered. Enjoy your purchase!");
            req.setNotificationType("ORDER_DELIVERED");
            pushService.sendPush(req);
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "notification-service")
    public void onOrderCancelled(Map<String, Object> event) {
        String userId = getString(event, "userId");
        String email  = getString(event, "customerEmail");
        String orderId = getString(event, "orderId");
        if (email != null) {
            EmailRequest req = new EmailRequest();
            req.setUserId(userId);
            req.setTo(email);
            req.setSubject("Order Cancelled – " + orderId);
            req.setBody("<p>Your order <b>#" + orderId + "</b> has been cancelled.</p>");
            req.setNotificationType("ORDER_CANCELLED");
            emailService.sendEmail(req);
        }
    }

    // ---- Payment events ----
    @KafkaListener(topics = "payment.success", groupId = "notification-service")
    public void onPaymentSuccess(Map<String, Object> event) {
        String userId = getString(event, "userId");
        String email  = getString(event, "customerEmail");
        String amount = getString(event, "amount");
        if (email != null) {
            EmailRequest req = new EmailRequest();
            req.setUserId(userId);
            req.setTo(email);
            req.setSubject("Payment Successful – ₹" + amount);
            req.setBody("<p>Payment of <b>₹" + amount + "</b> received. Thank you!</p>");
            req.setNotificationType("PAYMENT_SUCCESS");
            emailService.sendEmail(req);
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service")
    public void onPaymentFailed(Map<String, Object> event) {
        String userId = getString(event, "userId");
        String email  = getString(event, "customerEmail");
        if (email != null) {
            EmailRequest req = new EmailRequest();
            req.setUserId(userId);
            req.setTo(email);
            req.setSubject("Payment Failed – Action Required");
            req.setBody("<p>Your payment could not be processed. Please retry.</p>");
            req.setNotificationType("PAYMENT_FAILED");
            emailService.sendEmail(req);
        }
    }

    // ---- OTP event ----
    @KafkaListener(topics = "notification.otp", groupId = "notification-service")
    public void onOtp(Map<String, Object> event) {
        String phone = getString(event, "phone");
        String otp   = getString(event, "otp");
        String userId = getString(event, "userId");
        if (phone != null && otp != null) {
            SmsRequest req = new SmsRequest();
            req.setUserId(userId);
            req.setTo(phone);
            req.setMessage("NearKart OTP: " + otp + ". Valid for 5 minutes. Do not share.");
            req.setNotificationType("OTP");
            smsService.sendSms(req);
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
