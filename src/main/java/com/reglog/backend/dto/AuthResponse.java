package com.reglog.backend.dto;

public record AuthResponse(boolean authenticated, String message, UserResponse user) {}
