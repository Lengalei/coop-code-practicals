package com.coop.payments.messaging;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferCompletedEvent(String reference, String fromAccount, String toAccount,
                                     BigDecimal amount, String currency, Instant occurredAt) {
}
