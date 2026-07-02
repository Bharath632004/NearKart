package com.nearkart.userservice.exception;

public class AccountDeactivatedException extends RuntimeException {
    public AccountDeactivatedException() {
        super("Account is deactivated. Please contact support.");
    }
}
