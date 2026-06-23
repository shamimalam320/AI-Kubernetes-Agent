package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result of network and service inspection.
 */
@Data
@Builder
public class NetworkInspectionResult {
    
    /**
     * Whether all services are healthy
     */
    private boolean healthy;
    
    /**
     * List of services with issues
     */
    private List<ServiceIssue> serviceIssues;
    
    /**
     * Total number of services
     */
    private int totalServices;
    
    /**
     * Number of healthy services
     */
    private int healthyServices;
    
    /**
     * Number of services with issues
     */
    private int servicesWithIssues;
}

// Made with Bob
