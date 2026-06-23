package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result of pod inspection across the Kubernetes cluster.
 */
@Data
@Builder
public class PodInspectionResult {
    
    /**
     * Whether all pods are healthy
     */
    private boolean healthy;
    
    /**
     * List of pods with detected issues
     */
    private List<ProblematicPod> problematicPods;
    
    /**
     * Total number of pods in the cluster
     */
    private int totalPods;
    
    /**
     * Number of healthy pods
     */
    private int healthyPods;
    
    /**
     * Number of problematic pods
     */
    private int problematicPodsCount;
}

// Made with Bob
