package com.reglog.backend.dto;

import java.time.Instant;

public record UserResponse(Long id, String fullName, String email, String mobileNumber, String role, Instant createdAt) {}
