package com.reglog.backend.dto;

import jakarta.validation.constraints.*;

public record VerifyOtpRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits") String otp
) {}
