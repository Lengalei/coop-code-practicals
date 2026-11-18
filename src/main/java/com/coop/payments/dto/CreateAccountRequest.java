package com.coop.payments.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String ownerName,
        @PositiveOrZero @Digits(integer = 13, fraction = 2) BigDecimal openingBalance) {
}
