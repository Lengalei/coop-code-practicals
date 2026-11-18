package com.coop.payments.dto;

import java.math.BigDecimal;
import java.time.Instant;


public record BalanceResponse(String accountNumber, String ownerName, String currency,
                              BigDecimal balance, String formattedBalance, Instant asAt) {
}
