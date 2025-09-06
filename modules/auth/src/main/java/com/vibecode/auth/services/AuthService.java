package com.vibecode.auth.services;

import com.vibecode.auth.dto.request.LoginRequest;
import com.vibecode.auth.dto.request.RegisterRequest;
import com.vibecode.auth.dto.response.AuthResponse;
import com.vibecode.auth.models.User;
import com.vibecode.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .banned(false)
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                Map.of("uid", user.getId(), "name", user.getName())
        );
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getName(), user.isBanned());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                Map.of("uid", user.getId(), "name", user.getName())
        );
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getName(), user.isBanned());
    }
}

