package com.nearkart.userservice.exception;

public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException(String phone) {
        super("Phone number already registered: " + phone);
    }
}
