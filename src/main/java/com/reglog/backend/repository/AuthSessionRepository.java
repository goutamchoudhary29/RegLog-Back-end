package com.reglog.backend.repository;

import com.reglog.backend.entity.AuthSession;
import com.reglog.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findByTokenId(String tokenId);
    List<AuthSession> findByUserAndRevokedFalse(User user);
    @Modifying
    @Query("UPDATE AuthSession s SET s.revoked = true WHERE s.user = :user")
    int revokeAllByUser(@Param("user") User user);
}
