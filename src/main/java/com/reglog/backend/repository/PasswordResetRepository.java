package com.reglog.backend.repository;

import com.reglog.backend.entity.PasswordReset;
import com.reglog.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByUser(User user);
    Optional<PasswordReset> findByResetTokenHash(String resetTokenHash);
}
