package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

/**
 * Container for pod logs collected during investigation.
 */
@Data
@Builder
public class PodLogs {
    
    /**
     * Name of the pod
     */
    private String podName;
    
    /**
     * Namespace of the pod
     */
    private String namespace;
    
    /**
     * Container name within the pod
     */
    private String containerName;
    
    /**
     * Collected log content (last 100 lines or error-focused)
     */
    private String logs;
    
    /**
     * Whether logs contain errors
     */
    private boolean hasErrors;
    
    /**
     * Number of log lines collected
     */
    private int lineCount;
}

// Made with Bob
