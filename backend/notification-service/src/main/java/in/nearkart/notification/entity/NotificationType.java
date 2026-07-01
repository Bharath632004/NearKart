package in.nearkart.notification.entity;

public enum NotificationType {
    // Order lifecycle
    ORDER_PLACED,
    ORDER_CONFIRMED,
    ORDER_PREPARING,
    ORDER_READY_FOR_PICKUP,
    ORDER_PICKED_UP,
    ORDER_OUT_FOR_DELIVERY,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    // Payment
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    REFUND_INITIATED,
    // Wallet
    WALLET_CREDITED,
    WALLET_DEBITED,
    // Misc
    OTP,
    PROMO,
    GENERAL
}
