package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result of Kubernetes events analysis.
 */
@Data
@Builder
public class EventsAnalysisResult {
    
    /**
     * Whether the cluster is healthy based on events
     */
    private boolean healthy;
    
    /**
     * List of warning and error events
     */
    private List<KubernetesEvent> criticalEvents;
    
    /**
     * Total number of events analyzed
     */
    private int totalEvents;
    
    /**
     * Number of critical events
     */
    private int criticalEventsCount;
    
    /**
     * Most common event reason
     */
    private String mostCommonReason;
}
