package in.nearkart.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class SignatureVerificationException extends RuntimeException {
    public SignatureVerificationException(String message) { super(message); }
}
