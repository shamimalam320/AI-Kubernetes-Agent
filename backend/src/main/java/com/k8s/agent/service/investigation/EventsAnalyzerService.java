package com.k8s.agent.service.investigation;

import com.k8s.agent.dto.investigation.EventsAnalysisResult;
import com.k8s.agent.dto.investigation.KubernetesEvent;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.CoreV1Event;
import io.kubernetes.client.openapi.models.CoreV1EventList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for analyzing Kubernetes events to detect cluster issues.
 * Focuses on warning and error events from the last hour.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventsAnalyzerService {

    private final CoreV1Api coreV1Api;
    
    private static final int EVENT_LOOKBACK_SECONDS = 3600; // 1 hour

    /**
     * Analyzes Kubernetes events across all namespaces.
     *
     * @return EventsAnalysisResult containing critical events and statistics
     */
    public EventsAnalysisResult analyzeEvents() {
        log.info("Starting Kubernetes events analysis");
        
        List<KubernetesEvent> criticalEvents = new ArrayList<>();
        int totalEvents = 0;
        int warningCount = 0;
        int errorCount = 0;
        Map<String, Integer> reasonCounts = new HashMap<>();
        
        try {
            // Get all events across all namespaces
            CoreV1EventList eventList = coreV1Api.listEventForAllNamespaces(
                null, null, null, null, null, null, null, null, null, null, null
            );
            
            totalEvents = eventList.getItems().size();
            log.debug("Found {} total events in cluster", totalEvents);
            
            // Filter and analyze recent events
            OffsetDateTime cutoffTime = OffsetDateTime.now().minusSeconds(EVENT_LOOKBACK_SECONDS);
            
            for (CoreV1Event event : eventList.getItems()) {
                // Check if event is recent
                OffsetDateTime eventTime = event.getLastTimestamp() != null ? 
                    event.getLastTimestamp() : event.getFirstTimestamp();
                
                if (eventTime != null && eventTime.isAfter(cutoffTime)) {
                    String type = event.getType();
                    String reason = event.getReason();
                    
                    // Count event types
                    if ("Warning".equals(type)) {
                        warningCount++;
                    } else if ("Error".equals(type)) {
                        errorCount++;
                    }
                    
                    // Track reason frequency
                    reasonCounts.put(reason, reasonCounts.getOrDefault(reason, 0) + 1);
                    
                    // Collect critical events
                    if (isCriticalEvent(type, reason)) {
                        criticalEvents.add(convertToKubernetesEvent(event));
                        log.debug("Detected critical event: {} - {}", reason, event.getMessage());
                    }
                }
            }
            
            // Find most common reason
            String mostCommonReason = reasonCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
            
            boolean hasCriticalIssues = !criticalEvents.isEmpty();
            
            boolean healthy = criticalEvents.isEmpty();
            
            log.info("Events analysis complete: {} total, {} critical, healthy: {}",
                totalEvents, criticalEvents.size(), healthy);
            
            return EventsAnalysisResult.builder()
                .healthy(healthy)
                .criticalEvents(criticalEvents)
                .totalEvents(totalEvents)
                .criticalEventsCount(criticalEvents.size())
                .mostCommonReason(mostCommonReason)
                .build();
                
        } catch (ApiException e) {
            log.error("Failed to analyze events: {} - {}", e.getCode(), e.getResponseBody(), e);
            throw new RuntimeException("Failed to analyze events: " + e.getMessage(), e);
        }
    }

    /**
     * Determines if an event is critical based on type and reason.
     *
     * @param type event type (Warning, Error, Normal)
     * @param reason event reason
     * @return true if event is critical
     */
    private boolean isCriticalEvent(String type, String reason) {
        if (!"Warning".equals(type) && !"Error".equals(type)) {
            return false;
        }
        
        // Critical event reasons
        return "FailedScheduling".equals(reason) ||
               "BackOff".equals(reason) ||
               "FailedMount".equals(reason) ||
               "FailedAttachVolume".equals(reason) ||
               "FailedPull".equals(reason) ||
               "ErrImagePull".equals(reason) ||
               "ImagePullBackOff".equals(reason) ||
               "Unhealthy".equals(reason) ||
               "FailedCreate".equals(reason) ||
               "FailedKillPod".equals(reason) ||
               "NetworkNotReady".equals(reason) ||
               "InsufficientMemory".equals(reason) ||
               "InsufficientCPU".equals(reason) ||
               "NodeNotReady".equals(reason);
    }

    /**
     * Converts Kubernetes CoreV1Event to our KubernetesEvent DTO.
     *
     * @param event the Kubernetes event
     * @return KubernetesEvent DTO
     */
    private KubernetesEvent convertToKubernetesEvent(CoreV1Event event) {
        LocalDateTime firstTimestamp = event.getFirstTimestamp() != null ?
            LocalDateTime.ofInstant(event.getFirstTimestamp().toInstant(), ZoneId.systemDefault()) :
            LocalDateTime.now();
            
        LocalDateTime lastTimestamp = event.getLastTimestamp() != null ?
            LocalDateTime.ofInstant(event.getLastTimestamp().toInstant(), ZoneId.systemDefault()) :
            firstTimestamp;
        
        String involvedObject = event.getInvolvedObject() != null ?
            event.getInvolvedObject().getKind() + "/" + event.getInvolvedObject().getName() :
            "Unknown";
        
        String namespace = event.getInvolvedObject() != null ?
            event.getInvolvedObject().getNamespace() :
            event.getMetadata().getNamespace();
        
        return KubernetesEvent.builder()
            .type(event.getType())
            .reason(event.getReason())
            .message(event.getMessage())
            .involvedObject(involvedObject)
            .namespace(namespace)
            .count(event.getCount() != null ? event.getCount() : 1)
            .firstTimestamp(firstTimestamp)
            .lastTimestamp(lastTimestamp)
            .build();
    }
}

// Made with Bob
