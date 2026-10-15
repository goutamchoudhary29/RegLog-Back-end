package com.reglog.backend.dto;

public record VerifyOtpResponse(String resetToken, long expiresInMinutes, String message) {}
