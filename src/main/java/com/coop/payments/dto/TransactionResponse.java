package com.coop.payments.dto;

import com.coop.payments.entity.Channel;
import com.coop.payments.entity.CustomerTransaction;
import com.coop.payments.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(String reference, String accountNumber, TransactionType type, Channel channel,
                                  BigDecimal amount, BigDecimal balanceAfter, Instant createdAt) {
    public static TransactionResponse from(CustomerTransaction t) {
        return new TransactionResponse(t.getReference(), t.getAccountNumber(), t.getType(), t.getChannel(),
                t.getAmount(), t.getBalanceAfter(), t.getCreatedAt());
    }
}
