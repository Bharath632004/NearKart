package in.nearkart.payment.entity;

public enum PaymentStatus {
    CREATED,
    ATTEMPTED,
    SUCCESS,
    FAILED,
    REFUND_INITIATED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
