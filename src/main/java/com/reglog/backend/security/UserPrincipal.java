package com.reglog.backend.security;

public record UserPrincipal(Long id, String email, String fullName, String role) {}
