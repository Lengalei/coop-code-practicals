package com.coop.payments.dto;

public record AuthResponse(String token, String tokenType, long expiresInSeconds) {
}
