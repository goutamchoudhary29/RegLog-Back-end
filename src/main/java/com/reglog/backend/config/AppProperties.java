package com.reglog.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    public Jwt jwt = new Jwt();
    public Cookie cookie = new Cookie();
    public Cors cors = new Cors();
    public Frontend frontend = new Frontend();
    public Mail mail = new Mail();

    public static class Jwt {
        private String secret = "dev-only-change-me-please-use-32-plus-bytes-secret";
        private String issuer = "reglog-backend";
        private long accessTokenMinutes = 30;
        private String cookieName = "access_token";

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public long getAccessTokenMinutes() { return accessTokenMinutes; }
        public void setAccessTokenMinutes(long accessTokenMinutes) { this.accessTokenMinutes = accessTokenMinutes; }
        public String getCookieName() { return cookieName; }
        public void setCookieName(String cookieName) { this.cookieName = cookieName; }
    }

    public static class Cookie {
        private boolean secure = false;
        private String sameSite = "Lax";
        private String path = "/";

        public boolean isSecure() { return secure; }
        public void setSecure(boolean secure) { this.secure = secure; }
        public String getSameSite() { return sameSite; }
        public void setSameSite(String sameSite) { this.sameSite = sameSite; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }

    public static class Cors {
        private String allowedOrigin = "http://localhost:5173";

        public String getAllowedOrigin() { return allowedOrigin; }
        public void setAllowedOrigin(String allowedOrigin) { this.allowedOrigin = allowedOrigin; }
    }

    public static class Frontend {
        private String baseUrl = "http://localhost:5173";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class Mail {
        private String from = "no-reply@reglog.local";

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
    }
}
