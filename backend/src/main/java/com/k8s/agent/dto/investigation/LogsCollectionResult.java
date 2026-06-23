package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result of logs collection from problematic pods.
 */
@Data
@Builder
public class LogsCollectionResult {
    
    /**
     * List of collected pod logs
     */
    private List<PodLogs> podLogs;
    
    /**
     * Total number of pods for which logs were collected
     */
    private int totalPodsChecked;
    
    /**
     * Number of pods with error logs
     */
    private int podsWithErrorLogs;
    
    /**
     * Whether any critical errors were found
     */
    private boolean hasCriticalErrors;
}
