package com.k8s.agent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Investigation entity for storing investigation history.
 * Stores Kubernetes investigation results and AI diagnosis in PostgreSQL.
 */
@Entity
@Table(name = "investigations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Investigation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String rootCause;
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @Column(columnDefinition = "TEXT")
    private String suggestedFix;
    
    @Column(nullable = false)
    private Integer confidence;
    
    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, IN_PROGRESS
    
    @Column(columnDefinition = "TEXT")
    private String investigationData; // Full investigation result as JSON
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Set creation timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

// Made with Bob
