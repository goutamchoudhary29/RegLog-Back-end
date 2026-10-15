package com.reglog.backend.service;

import com.reglog.backend.dto.AuthResponse;
import com.reglog.backend.dto.ChangePasswordRequest;
import com.reglog.backend.dto.LoginRequest;
import com.reglog.backend.dto.UserResponse;
import com.reglog.backend.entity.AuthSession;
import com.reglog.backend.entity.User;
import com.reglog.backend.exception.BadRequestException;
import com.reglog.backend.exception.InvalidCredentialsException;
import com.reglog.backend.repository.AuthSessionRepository;
import com.reglog.backend.repository.UserRepository;
import com.reglog.backend.security.AuthCookieUtil;
import com.reglog.backend.security.JwtService;
import com.reglog.backend.security.UserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthSessionRepository sessionRepository;
    private final JwtService jwtService;
    private final AuthCookieUtil cookieUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthService(UserRepository userRepository,
                       AuthSessionRepository sessionRepository,
                       JwtService jwtService,
                       AuthCookieUtil cookieUtil,
                       PasswordEncoder passwordEncoder,
                       UserService userService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.jwtService = jwtService;
        this.cookieUtil = cookieUtil;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmailOrMobileNumber(request.identifier(), request.identifier()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        String jti = jwtService.newTokenId();
        String token = jwtService.generateToken(user, jti);
        JwtService.TokenClaims claims = jwtService.parseAndValidate(token);
        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setTokenId(jti);
        session.setExpiresAt(claims.expiresAt());
        sessionRepository.save(session);

        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(token).toString());
        UserResponse userResponse = userService.toResponse(user);
        return new AuthResponse(true, "Login successful", userResponse);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = extractCookie(request, cookieUtil.getCookieName());
        if (token != null && !token.isBlank()) {
            JwtService.TokenClaims claims = jwtService.parseAndValidate(token);
            if (claims != null) {
                sessionRepository.findByTokenId(claims.jti()).ifPresent(session -> {
                    session.setRevoked(true);
                    sessionRepository.save(session);
                });
            }
        }
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.clearAccessTokenCookie().toString());
    }

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request, HttpServletResponse response) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        User user = userRepository.findById(principal.id()).orElseThrow(() -> new BadRequestException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        sessionRepository.revokeAllByUser(user);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.clearAccessTokenCookie().toString());
    }

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
