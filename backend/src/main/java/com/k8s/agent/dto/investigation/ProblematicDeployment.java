package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a deployment with detected issues.
 */
@Data
@Builder
public class ProblematicDeployment {
    
    /**
     * Name of the deployment
     */
    private String name;
    
    /**
     * Namespace of the deployment
     */
    private String namespace;
    
    /**
     * Desired number of replicas
     */
    private int desiredReplicas;
    
    /**
     * Number of available replicas
     */
    private int availableReplicas;
    
    /**
     * Number of unavailable replicas
     */
    private int unavailableReplicas;
    
    /**
     * Deployment status/condition
     */
    private String status;
    
    /**
     * Reason for the issue
     */
    private String reason;
    
    /**
     * Additional message
     */
    private String message;
}

// Made with Bob
