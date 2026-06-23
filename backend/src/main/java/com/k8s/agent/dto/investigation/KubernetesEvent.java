package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a Kubernetes event detected during investigation.
 */
@Data
@Builder
public class KubernetesEvent {
    
    /**
     * Type of event (Warning, Error, Normal)
     */
    private String type;
    
    /**
     * Reason for the event
     */
    private String reason;
    
    /**
     * Event message
     */
    private String message;
    
    /**
     * Involved object (pod, deployment, etc.)
     */
    private String involvedObject;
    
    /**
     * Namespace of the involved object
     */
    private String namespace;
    
    /**
     * Number of times this event occurred
     */
    private int count;
    
    /**
     * First occurrence timestamp
     */
    private LocalDateTime firstTimestamp;
    
    /**
     * Last occurrence timestamp
     */
    private LocalDateTime lastTimestamp;
}

// Made with Bob
