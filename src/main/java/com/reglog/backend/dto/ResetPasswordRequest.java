package com.reglog.backend.dto;

import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
        @NotBlank String resetToken,
        @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$", message = "Weak password") String newPassword,
        @NotBlank String confirmPassword
) {}
