package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Comprehensive result of Kubernetes cluster investigation.
 * Combines all investigation components into a single response.
 */
@Data
@Builder
public class InvestigationResult {
    
    /**
     * Timestamp when investigation was performed
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Overall cluster health status
     */
    private boolean clusterHealthy;
    
    /**
     * Pod inspection results
     */
    private PodInspectionResult podInspection;
    
    /**
     * Logs collection results
     */
    private LogsCollectionResult logsCollection;
    
    /**
     * Events analysis results
     */
    private EventsAnalysisResult eventsAnalysis;
    
    /**
     * Deployment inspection results
     */
    private DeploymentInspectionResult deploymentInspection;
    
    /**
     * Network inspection results
     */
    private NetworkInspectionResult networkInspection;
    
    /**
     * Duration of investigation in seconds
     */
    private long investigationDurationSeconds;
}
