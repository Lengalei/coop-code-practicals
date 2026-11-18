package com.coop.payments.exception;

import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends ApiException {
    public AccountNotFoundException(String accountNumber) {
        super(HttpStatus.NOT_FOUND, "Account not found: " + accountNumber);
    }
}
