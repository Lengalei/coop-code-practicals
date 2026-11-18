package com.coop.payments.dto;

import com.coop.payments.entity.Channel;
import com.coop.payments.entity.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank @Size(max = 40) String reference,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "must be a 10-digit account number") String accountNumber,
        @NotNull TransactionType type,
        @NotNull Channel channel,
        @NotNull @DecimalMin(value = "0.01", message = "must be at least 0.01")
        @Digits(integer = 13, fraction = 2, message = "max 2 decimal places") BigDecimal amount,
        @Pattern(regexp = "^254\\d{9}$", message = "must be in format 254XXXXXXXXX") String msisdn,
        @Size(max = 200) String description) {
}
