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

    public record TokenClaims(Long userId, String email, String fullName, String jti, Instant expiresAt) {}

    public String generateToken(User user, String jti) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.jwt.getAccessTokenMinutes() * 60);
        SecretKey key = signingKey();
        return Jwts.builder()
                .issuer(properties.jwt.getIssuer())
                .subject(user.getId().toString())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
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
            if (claims.getSubject() == null || claims.getId() == null || claims.getExpiration() == null) {
                return null;
            }
            Long userId = Long.valueOf(claims.getSubject());
            Instant expiry = claims.getExpiration().toInstant();
            if (expiry.isBefore(Instant.now())) {
                return null;
            }
            return new TokenClaims(userId, claims.get("email", String.class), claims.get("fullName", String.class), claims.getId(), expiry);
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
