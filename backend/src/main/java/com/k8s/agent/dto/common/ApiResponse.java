package com.k8s.agent.dto.common;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for all REST endpoints.
 *
 * @param <T> the type of data being returned
 */
@Data
@Builder
public class ApiResponse<T> {
    
    /**
     * Whether the request was successful
     */
    private boolean success;
    
    /**
     * Human-readable message describing the result
     */
    private String message;
    
    /**
     * The actual data payload
     */
    private T data;
    
    /**
     * Timestamp when the response was generated
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
