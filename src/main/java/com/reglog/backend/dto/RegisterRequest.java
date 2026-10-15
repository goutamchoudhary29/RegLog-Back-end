package com.reglog.backend.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
        String mobileNumber,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character")
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {}
