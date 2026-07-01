package com.nearkart.auth.service;

import com.nearkart.auth.dto.AuthResponse;
import com.nearkart.auth.dto.LoginRequest;
import com.nearkart.auth.dto.RegisterRequest;
import com.nearkart.auth.entity.User;
import com.nearkart.auth.repository.UserRepository;
import com.nearkart.auth.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        String role = (request.getRole() == null || request.getRole().isBlank())
                ? "CUSTOMER"
                : request.getRole().toUpperCase();

        User user = new User(
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        userRepository.save(user);

        return new AuthResponse(jwtUtil.generateToken(user.getPhone(), user.getRole()));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("Invalid phone or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid phone or password");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("Account is disabled. Contact support.");
        }

        return new AuthResponse(jwtUtil.generateToken(user.getPhone(), user.getRole()));
    }
}
