package com.nearkart.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class OrderCannotBeReturnedException extends RuntimeException {
    public OrderCannotBeReturnedException(String message) {
        super(message);
    }
}
