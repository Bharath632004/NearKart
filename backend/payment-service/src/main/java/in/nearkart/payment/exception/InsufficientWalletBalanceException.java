package in.nearkart.payment.exception;
public class InsufficientWalletBalanceException extends RuntimeException {
    public InsufficientWalletBalanceException(String msg) { super(msg); }
}
