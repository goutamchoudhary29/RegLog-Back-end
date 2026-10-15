package com.reglog.backend.service;

import com.reglog.backend.dto.ForgotPasswordRequest;
import com.reglog.backend.dto.MessageResponse;
import com.reglog.backend.dto.ResetPasswordRequest;
import com.reglog.backend.dto.VerifyOtpResponse;
import com.reglog.backend.entity.PasswordReset;
import com.reglog.backend.entity.User;
import com.reglog.backend.exception.BadRequestException;
import com.reglog.backend.exception.TooManyRequestsException;
import com.reglog.backend.repository.PasswordResetRepository;
import com.reglog.backend.repository.UserRepository;
import com.reglog.backend.repository.AuthSessionRepository;
import com.reglog.backend.util.SecureHashUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PasswordResetService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long OTP_EXPIRY_MINUTES = 5;
    private static final long RESET_EXPIRY_MINUTES = 10;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionRepository sessionRepository;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetRepository passwordResetRepository,
                                EmailService emailService,
                                PasswordEncoder passwordEncoder,
                                AuthSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user != null) {
            PasswordReset reset = passwordResetRepository.findByUser(user).orElseGet(() -> {
                PasswordReset created = new PasswordReset();
                created.setUser(user);
                return created;
            });
            reset.setUser(user);
            Instant now = Instant.now();
            if (reset.getLastSentAt() != null && reset.getLastSentAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(now)) {
                throw new TooManyRequestsException("Please wait before requesting another OTP");
            }
            String otp = SecureHashUtil.randomOtp();
            reset.setOtpHash(SecureHashUtil.sha256Hex(otp));
            reset.setOtpExpiresAt(now.plusSeconds(OTP_EXPIRY_MINUTES * 60));
            reset.setAttempts(0);
            reset.setLastSentAt(now);
            reset.setResetTokenHash(null);
            reset.setResetExpiresAt(null);
            passwordResetRepository.save(reset);
            emailService.sendOtp(user.getEmail(), otp);
        }
        return new MessageResponse("If an account exists for that email, an OTP has been sent.");
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(com.reglog.backend.dto.VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null) {
            throw new BadRequestException("Invalid or expired OTP");
        }
        PasswordReset reset = passwordResetRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        if (reset.getAttempts() >= MAX_ATTEMPTS) {
            throw new TooManyRequestsException("Too many OTP attempts");
        }
        if (reset.getOtpHash() == null || reset.getOtpExpiresAt() == null || reset.getOtpExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        if (!SecureHashUtil.constantTimeEquals(reset.getOtpHash(), SecureHashUtil.sha256Hex(request.otp()))) {
            reset.setAttempts(reset.getAttempts() + 1);
            passwordResetRepository.save(reset);
            throw new BadRequestException("Invalid or expired OTP");
        }

        reset.setOtpHash(null);
        reset.setOtpExpiresAt(null);
        reset.setAttempts(0);
        String resetToken = SecureHashUtil.randomToken();
        reset.setResetTokenHash(SecureHashUtil.sha256Hex(resetToken));
        reset.setResetExpiresAt(Instant.now().plusSeconds(RESET_EXPIRY_MINUTES * 60));
        passwordResetRepository.save(reset);

        return new VerifyOtpResponse(resetToken, RESET_EXPIRY_MINUTES, "OTP verified successfully");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        PasswordReset reset = passwordResetRepository.findByResetTokenHash(SecureHashUtil.sha256Hex(request.resetToken()))
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if (reset.getResetExpiresAt() == null || reset.getResetExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = reset.getUser();
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        passwordResetRepository.delete(reset);
        sessionRepository.revokeAllByUser(user);
        return new MessageResponse("Password reset successfully. Please log in again.");
    }
}
