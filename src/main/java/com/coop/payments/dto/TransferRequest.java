package com.coop.payments.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank @Size(max = 40) String reference,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "must be a 10-digit account number") String fromAccount,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "must be a 10-digit account number") String toAccount,
        @NotNull @DecimalMin(value = "0.01", message = "must be at least 0.01")
        @Digits(integer = 13, fraction = 2, message = "max 2 decimal places") BigDecimal amount,
        @Size(max = 200) String description) {
}
