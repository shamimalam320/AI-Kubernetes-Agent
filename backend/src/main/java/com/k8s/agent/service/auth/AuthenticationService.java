package com.k8s.agent.service.auth;

import com.k8s.agent.dto.auth.LoginRequest;
import com.k8s.agent.dto.auth.RegisterRequest;
import com.k8s.agent.dto.auth.AuthResponse;
import com.k8s.agent.entity.User;
import com.k8s.agent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for user authentication operations.
 * Handles registration, login, and JWT token generation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Register a new user.
     *
     * @param request Registration request
     * @return Authentication response with JWT token
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .enabled(true)
                .build();
        
        userRepository.save(user);
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getUsername());
        
        log.info("User registered successfully: {}", user.getUsername());
        
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .message("Registration successful")
                .build();
    }
    
    /**
     * Authenticate user and generate JWT token.
     *
     * @param request Login request
     * @return Authentication response with JWT token
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());
        
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        // Load user details
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getUsername());
        
        log.info("User authenticated successfully: {}", user.getUsername());
        
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .message("Login successful")
                .build();
    }
}

// Made with Bob
