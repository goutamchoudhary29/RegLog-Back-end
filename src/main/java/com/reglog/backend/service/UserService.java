package com.reglog.backend.service;

import com.reglog.backend.dto.UserResponse;
import com.reglog.backend.entity.User;
import com.reglog.backend.exception.ConflictException;
import com.reglog.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String fullName, String email, String mobileNumber, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }
        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new ConflictException("Mobile number is already registered");
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setMobileNumber(mobileNumber);
        user.setPasswordHash(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getMobileNumber(), user.getCreatedAt());
    }
}
