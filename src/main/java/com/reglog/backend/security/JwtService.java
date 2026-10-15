package com.reglog.backend.security;

import com.reglog.backend.config.AppProperties;
import com.reglog.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final AppProperties properties;

    public JwtService(AppProperties properties) {
        this.properties = properties;
    }

    public record TokenClaims(Long userId, String username, String email, String fullName, String role, String jti, Instant expiresAt) {}

    public String generateToken(User user, String jti) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.jwt.getAccessTokenMinutes() * 60);
        SecretKey key = signingKey();
        String role = (user.getRole() != null && !user.getRole().isBlank()) ? user.getRole() : "ROLE_USER";
        return Jwts.builder()
                .issuer(properties.jwt.getIssuer())
                .subject(user.getEmail())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("userId", user.getId())
                .claim("username", user.getEmail())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .claim("role", role)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public TokenClaims parseAndValidate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .requireIssuer(properties.jwt.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getId() == null || claims.getExpiration() == null) {
                return null;
            }
            Long userId = claims.get("userId", Long.class);
            if (userId == null && claims.getSubject() != null) {
                try {
                    userId = Long.valueOf(claims.getSubject());
                } catch (NumberFormatException ignored) {}
            }
            if (userId == null) {
                return null;
            }
            Instant expiry = claims.getExpiration().toInstant();
            if (expiry.isBefore(Instant.now())) {
                return null;
            }
            String email = claims.get("email", String.class);
            if (email == null) {
                email = claims.getSubject();
            }
            String username = claims.get("username", String.class);
            if (username == null) {
                username = email;
            }
            String role = claims.get("role", String.class);
            if (role == null) {
                role = "ROLE_USER";
            }
            String fullName = claims.get("fullName", String.class);
            return new TokenClaims(userId, username, email, fullName, role, claims.getId(), expiry);
        } catch (Exception ex) {
            return null;
        }
    }

    public String newTokenId() {
        return UUID.randomUUID().toString();
    }

    private SecretKey signingKey() {
        String secret = properties.jwt.getSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
