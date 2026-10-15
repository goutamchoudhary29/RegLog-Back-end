package com.reglog.backend.dto;

import jakarta.validation.constraints.*;

public record ForgotPasswordRequest(
        @NotBlank @Email @Size(max = 255) String email
) {}
