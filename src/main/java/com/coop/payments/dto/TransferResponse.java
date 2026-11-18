package com.coop.payments.dto;

import com.coop.payments.entity.Transfer;
import com.coop.payments.entity.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(String reference, String fromAccount, String toAccount, BigDecimal amount,
                               TransferStatus status, Instant createdAt) {
    public static TransferResponse from(Transfer t) {
        return new TransferResponse(t.getReference(), t.getSourceAccount(), t.getDestinationAccount(),
                t.getAmount(), t.getStatus(), t.getCreatedAt());
    }
}
