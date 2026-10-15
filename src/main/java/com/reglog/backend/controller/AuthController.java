package com.reglog.backend.controller;

import com.reglog.backend.dto.AuthResponse;
import com.reglog.backend.dto.ChangePasswordRequest;
import com.reglog.backend.dto.ForgotPasswordRequest;
import com.reglog.backend.dto.LoginRequest;
import com.reglog.backend.dto.MessageResponse;
import com.reglog.backend.dto.RegisterRequest;
import com.reglog.backend.dto.ResetPasswordRequest;
import com.reglog.backend.dto.UserResponse;
import com.reglog.backend.dto.VerifyOtpRequest;
import com.reglog.backend.dto.VerifyOtpResponse;
import com.reglog.backend.entity.User;
import com.reglog.backend.exception.BadRequestException;
import com.reglog.backend.security.UserPrincipal;
import com.reglog.backend.service.AuthService;
import com.reglog.backend.service.PasswordResetService;
import com.reglog.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, AuthService authService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        User user = userService.register(request.fullName(), request.email(), request.mobileNumber(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(false, "Registration successful", userService.toResponse(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.forgotPassword(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(passwordResetService.verifyOtp(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal,
                                                        HttpServletResponse response) {
        if (principal == null) {
            throw new BadRequestException("Authentication required");
        }
        authService.changePassword(principal, request, response);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully. Please log in again."));
    }
}
