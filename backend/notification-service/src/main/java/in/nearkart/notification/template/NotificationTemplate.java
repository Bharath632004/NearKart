package in.nearkart.notification.template;

import in.nearkart.notification.entity.NotificationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationTemplate {
    private String title;
    private String body;

    /**
     * Returns a pre-built template for each notification type.
     * Supports {orderNumber}, {amount}, {shopName} placeholders.
     */
    public static NotificationTemplate of(NotificationType type, String... args) {
        return switch (type) {
            case ORDER_PLACED          -> NotificationTemplate.builder()
                    .title("Order Placed! 🛍️")
                    .body("Your order #" + get(args,0) + " has been placed. Total: ₹" + get(args,1))
                    .build();
            case ORDER_CONFIRMED       -> NotificationTemplate.builder()
                    .title("Order Confirmed ✅")
                    .body("Order #" + get(args,0) + " confirmed by " + get(args,1) + ". Estimated delivery: 30 mins.")
                    .build();
            case ORDER_PREPARING       -> NotificationTemplate.builder()
                    .title("Order Being Prepared 👨‍🍳")
                    .body("Order #" + get(args,0) + " is being prepared by " + get(args,1) + ".")
                    .build();
            case ORDER_OUT_FOR_DELIVERY -> NotificationTemplate.builder()
                    .title("Out for Delivery 🛵")
                    .body("Your order #" + get(args,0) + " is on the way! Track it in the app.")
                    .build();
            case ORDER_DELIVERED       -> NotificationTemplate.builder()
                    .title("Delivered! 🎉")
                    .body("Order #" + get(args,0) + " delivered. Enjoy! Rate your experience.")
                    .build();
            case ORDER_CANCELLED       -> NotificationTemplate.builder()
                    .title("Order Cancelled ❌")
                    .body("Order #" + get(args,0) + " has been cancelled. Refund will be processed.")
                    .build();
            case PAYMENT_SUCCESS       -> NotificationTemplate.builder()
                    .title("Payment Successful 💳")
                    .body("₹" + get(args,0) + " paid successfully for order #" + get(args,1) + ".")
                    .build();
            case PAYMENT_FAILED        -> NotificationTemplate.builder()
                    .title("Payment Failed ⚠️")
                    .body("Payment for order #" + get(args,0) + " failed. Please retry.")
                    .build();
            case REFUND_INITIATED      -> NotificationTemplate.builder()
                    .title("Refund Initiated 🔄")
                    .body("₹" + get(args,0) + " refund initiated for order #" + get(args,1) + ". 5–7 business days.")
                    .build();
            case WALLET_CREDITED       -> NotificationTemplate.builder()
                    .title("Wallet Credited 💚")
                    .body("₹" + get(args,0) + " added to your NearKart wallet. New balance: ₹" + get(args,1))
                    .build();
            case WALLET_DEBITED        -> NotificationTemplate.builder()
                    .title("Wallet Debited")
                    .body("₹" + get(args,0) + " debited from wallet for order #" + get(args,1) + ".")
                    .build();
            default -> NotificationTemplate.builder()
                    .title("NearKart Update")
                    .body("You have a new update from NearKart.")
                    .build();
        };
    }

    private static String get(String[] args, int idx) {
        return (args != null && idx < args.length && args[idx] != null) ? args[idx] : "";
    }
}
