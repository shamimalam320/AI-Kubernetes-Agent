package com.k8s.agent.dto.websocket;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for WebSocket progress updates.
 * Sent to clients during investigation progress.
 */
@Data
@Builder
public class ProgressUpdate {
    
    /**
     * Investigation step name.
     * Examples: "Checking Pods", "Collecting Logs", "AI Reasoning"
     */
    private String step;
    
    /**
     * Step status.
     * Values: "IN_PROGRESS", "COMPLETED", "FAILED"
     */
    private String status;
    
    /**
     * Timestamp of the update.
     */
    private LocalDateTime timestamp;
    
    /**
     * Optional message with additional details.
     */
    private String message;
}

// Made with Bob
