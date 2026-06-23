package com.k8s.agent.controller;

import com.k8s.agent.dto.auth.AuthResponse;
import com.k8s.agent.dto.auth.LoginRequest;
import com.k8s.agent.dto.auth.RegisterRequest;
import com.k8s.agent.dto.common.ApiResponse;
import com.k8s.agent.service.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for user login and registration.
 * Handles local authentication with JWT tokens.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationService authenticationService;
    
    /**
     * Health check for auth service.
     *
     * @return Auth service status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .success(true)
                .message("Auth service is healthy")
                .data("OK")
                .build()
        );
    }
    
    /**
     * Register a new user.
     *
     * @param request Registration request with username, email, and password
     * @return Authentication response with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        log.info("Registration request for username: {}", request.getUsername());
        
        try {
            AuthResponse authResponse = authenticationService.register(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("User registered successfully")
                    .data(authResponse)
                    .build()
            );
        } catch (Exception e) {
            log.error("Registration failed for username: {}", request.getUsername(), e);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message("Registration failed: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Login endpoint.
     *
     * @param request Login request with username and password
     * @return Authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        
        try {
            AuthResponse authResponse = authenticationService.login(request);
            
            return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("Login successful")
                    .data(authResponse)
                    .build()
            );
        } catch (Exception e) {
            log.error("Login failed for username: {}", request.getUsername(), e);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message("Login failed: Invalid credentials")
                    .build()
            );
        }
    }
    
    /**
     * Validate JWT token endpoint.
     *
     * @return Validation status
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken() {
        // If this endpoint is reached, the JWT filter has already validated the token
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .success(true)
                .message("Token is valid")
                .data("OK")
                .build()
        );
    }
}

// Made with Bob
