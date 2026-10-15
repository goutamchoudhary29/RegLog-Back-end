package com.reglog.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email or mobile number is required")
        @Size(max = 255)
        String identifier,

        @NotBlank(message = "Password is required")
        String password
) {}
