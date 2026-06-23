package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result of deployment inspection.
 */
@Data
@Builder
public class DeploymentInspectionResult {
    
    /**
     * Whether all deployments are healthy
     */
    private boolean healthy;
    
    /**
     * List of deployments with issues
     */
    private List<ProblematicDeployment> problematicDeployments;
    
    /**
     * Total number of deployments
     */
    private int totalDeployments;
    
    /**
     * Number of healthy deployments
     */
    private int healthyDeployments;
    
    /**
     * Number of problematic deployments
     */
    private int problematicDeploymentsCount;
}

// Made with Bob
