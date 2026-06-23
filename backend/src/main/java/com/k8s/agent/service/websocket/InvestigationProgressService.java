package com.k8s.agent.service.websocket;

import com.k8s.agent.dto.websocket.ProgressUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for sending real-time investigation progress updates via WebSocket.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvestigationProgressService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send progress update to specific user.
     *
     * @param userId User ID
     * @param step Investigation step name
     * @param status Step status (IN_PROGRESS, COMPLETED, FAILED)
     */
    public void sendProgress(String userId, String step, String status) {
        sendProgress(userId, step, status, null);
    }
    
    /**
     * Send progress update with message to specific user.
     *
     * @param userId User ID
     * @param step Investigation step name
     * @param status Step status (IN_PROGRESS, COMPLETED, FAILED)
     * @param message Optional message
     */
    public void sendProgress(String userId, String step, String status, String message) {
        ProgressUpdate update = ProgressUpdate.builder()
                .step(step)
                .status(status)
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();
        
        try {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/topic/investigation-progress",
                    update
            );
            
            log.debug("Progress sent to user {}: {} - {}", userId, step, status);
        } catch (Exception e) {
            log.error("Failed to send progress update to user {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Send investigation started notification.
     *
     * @param userId User ID
     */
    public void sendInvestigationStarted(String userId) {
        sendProgress(userId, "Investigation Started", "IN_PROGRESS", 
                "Starting Kubernetes cluster investigation");
    }
    
    /**
     * Send investigation completed notification.
     *
     * @param userId User ID
     */
    public void sendInvestigationCompleted(String userId) {
        sendProgress(userId, "Investigation Completed", "COMPLETED", 
                "Root cause analysis complete");
    }
    
    /**
     * Send investigation failed notification.
     *
     * @param userId User ID
     * @param error Error message
     */
    public void sendInvestigationFailed(String userId, String error) {
        sendProgress(userId, "Investigation Failed", "FAILED", error);
    }
}

// Made with Bob
