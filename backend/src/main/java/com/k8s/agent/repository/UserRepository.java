package com.k8s.agent.repository;

import com.k8s.agent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by username.
     *
     * @param username Username
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email.
     *
     * @param email Email address
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists.
     *
     * @param username Username
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists.
     *
     * @param email Email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
}

// Made with Bob
