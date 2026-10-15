package com.reglog.backend.security;

import com.reglog.backend.config.AppProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AuthCookieUtil {
    private final AppProperties properties;

    public AuthCookieUtil(AppProperties properties) {
        this.properties = properties;
    }

    public String getCookieName() {
        return properties.jwt.getCookieName();
    }

    public ResponseCookie createAccessTokenCookie(String token) {
        int maxAgeSeconds = (int) TimeUnit.MINUTES.toSeconds(properties.jwt.getAccessTokenMinutes());
        return ResponseCookie.from(getCookieName(), token)
                .httpOnly(true)
                .secure(properties.cookie.isSecure())
                .path(properties.cookie.getPath())
                .sameSite(properties.cookie.getSameSite())
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from(getCookieName(), "")
                .httpOnly(true)
                .secure(properties.cookie.isSecure())
                .path(properties.cookie.getPath())
                .sameSite(properties.cookie.getSameSite())
                .maxAge(0)
                .build();
    }
}
