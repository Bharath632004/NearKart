package com.nearkart.userservice.service;

import com.nearkart.userservice.dto.*;
import com.nearkart.userservice.event.UserRegisteredEvent;
import com.nearkart.userservice.exception.*;
import com.nearkart.userservice.model.User;
import com.nearkart.userservice.repository.UserRepository;
import com.nearkart.userservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException(request.getPhone());
        }

        // SECURITY: Public registration always creates CUSTOMER role.
        // ADMIN and DELIVERY_AGENT must be assigned by admin via PUT /api/users/{id}/role
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.CUSTOMER)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: email={}, role={}", saved.getEmail(), saved.getRole());

        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getEmail(), saved.getName()));

        TokenPair tokens = jwtUtils.generateTokenPair(saved.getEmail(), saved.getRole().name());
        return new AuthResponse(
                tokens.accessToken(), tokens.refreshToken(),
                saved.getId(), saved.getName(), saved.getEmail(), saved.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidPasswordException();
        } catch (DisabledException e) {
            throw new AccountDeactivatedException();
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        if (!user.isActive()) {
            throw new AccountDeactivatedException();
        }

        log.info("User logged in: email={}", user.getEmail());
        TokenPair tokens = jwtUtils.generateTokenPair(user.getEmail(), user.getRole().name());
        return new AuthResponse(
                tokens.accessToken(), tokens.refreshToken(),
                user.getId(), user.getName(), user.getEmail(), user.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
        String email = jwtUtils.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (!user.isActive()) {
            throw new AccountDeactivatedException();
        }

        TokenPair tokens = jwtUtils.generateTokenPair(user.getEmail(), user.getRole().name());
        return new AuthResponse(
                tokens.accessToken(), tokens.refreshToken(),
                user.getId(), user.getName(), user.getEmail(), user.getRole().name()
        );
    }
}
