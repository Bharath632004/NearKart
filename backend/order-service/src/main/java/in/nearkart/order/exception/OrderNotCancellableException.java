package in.nearkart.order.exception;

public class OrderNotCancellableException extends RuntimeException {
    public OrderNotCancellableException(String message) { super(message); }
}
