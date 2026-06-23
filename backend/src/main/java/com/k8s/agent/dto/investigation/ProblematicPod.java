package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a pod with issues detected during investigation.
 */
@Data
@Builder
public class ProblematicPod {
    
    /**
     * Name of the pod
     */
    private String name;
    
    /**
     * Namespace where the pod is located
     */
    private String namespace;
    
    /**
     * Current status of the pod (e.g., CrashLoopBackOff, ImagePullBackOff)
     */
    private String status;
    
    /**
     * Reason for the current status
     */
    private String reason;
    
    /**
     * Number of times the pod has restarted
     */
    private int restartCount;
    
    /**
     * Additional message providing more context
     */
    private String message;
}

// Made with Bob
