package com.reglog.backend.security;

import com.reglog.backend.config.AppProperties;
import com.reglog.backend.entity.AuthSession;
import com.reglog.backend.entity.User;
import com.reglog.backend.repository.AuthSessionRepository;
import com.reglog.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AuthSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AppProperties properties;
    private final AuthCookieUtil cookieUtil;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   AuthSessionRepository sessionRepository,
                                   UserRepository userRepository,
                                   AppProperties properties,
                                   AuthCookieUtil cookieUtil) {
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.properties = properties;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractCookie(request, cookieUtil.getCookieName());
        if (token != null && !token.isBlank()) {
            JwtService.TokenClaims claims = jwtService.parseAndValidate(token);
            if (claims != null) {
                AuthSession session = sessionRepository.findByTokenId(claims.jti()).orElse(null);
                if (session != null && !session.isRevoked() && session.getExpiresAt().isAfter(Instant.now())) {
                    User user = userRepository.findById(claims.userId()).orElse(null);
                    if (user != null) {
                        String role = (user.getRole() != null && !user.getRole().isBlank()) ? user.getRole() : claims.role();
                        UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), user.getFullName(), role);
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(new SimpleGrantedAuthority(role)));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
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
