package com.k8s.agent.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Service for JWT token generation, validation and extraction.
 * Handles local JWT authentication.
 */
@Service
@Slf4j
public class JwtService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    /**
     * Generate JWT token for a user.
     *
     * @param username Username
     * @return Generated JWT token
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Extract username from JWT token.
     *
     * @param token JWT token
     * @return Username (subject claim)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract user ID from JWT token (alias for extractUsername).
     *
     * @param token JWT token
     * @return User ID (subject claim)
     */
    public String extractUserId(String token) {
        return extractUsername(token);
    }
    
    /**
     * Validate if token is valid for a specific username and not expired.
     *
     * @param token JWT token
     * @param username Username to validate against
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate if token is valid and not expired (without username check).
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token is expired.
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Extract expiration date from token.
     *
     * @param token JWT token
     * @return Expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract a specific claim from token.
     *
     * @param token JWT token
     * @param claimsResolver Function to extract claim
     * @param <T> Type of claim
     * @return Extracted claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from token.
     *
     * @param token JWT token
     * @return All claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Get signing key for JWT validation.
     *
     * @return Signing key
     */
    private Key getSigningKey() {
        // Use the secret directly as UTF-8 bytes instead of Base64 decoding
        // This allows any string to be used as the JWT secret
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

// Made with Bob
