package com.reglog.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageResponse(String message, String otp) {
    public MessageResponse(String message) {
        this(message, null);
    }
}
