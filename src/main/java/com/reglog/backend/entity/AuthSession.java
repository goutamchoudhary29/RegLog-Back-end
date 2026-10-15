package com.reglog.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auth_sessions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_auth_sessions_token_id", columnNames = "token_id")
}, indexes = {
        @Index(name = "idx_auth_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_auth_sessions_token_id", columnList = "token_id")
})
public class AuthSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_auth_sessions_user"))
    private User user;

    @Column(name = "token_id", nullable = false, length = 128)
    private String tokenId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTokenId() { return tokenId; }
    public void setTokenId(String tokenId) { this.tokenId = tokenId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}
