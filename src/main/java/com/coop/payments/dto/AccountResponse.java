package com.coop.payments.dto;

import com.coop.payments.entity.Account;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(String accountNumber, String ownerName, String currency,
                              BigDecimal balance, Instant createdAt) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getAccountNumber(), a.getOwnerName(), a.getCurrency(),
                a.getBalance(), a.getCreatedAt());
    }
}
