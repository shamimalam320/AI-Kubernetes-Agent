package com.k8s.agent.service.investigation;

import com.k8s.agent.dto.investigation.PodInspectionResult;
import com.k8s.agent.dto.investigation.ProblematicPod;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for inspecting Kubernetes pods across the cluster.
 * Detects unhealthy pods and categorizes issues.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PodInspectorService {

    private final CoreV1Api coreV1Api;

    /**
     * Inspects all pods in the cluster and identifies problematic ones.
     *
     * @return PodInspectionResult containing health status and problematic pods
     */
    public PodInspectionResult inspectPods() {
        log.info("Starting pod inspection across all namespaces");
        
        List<ProblematicPod> problematicPods = new ArrayList<>();
        int totalPods = 0;
        
        try {
            // Get all pods across all namespaces
            V1PodList podList = coreV1Api.listPodForAllNamespaces(
                null, null, null, null, null, null, null, null, null, null, null
            );
            
            totalPods = podList.getItems().size();
            log.debug("Found {} total pods in cluster", totalPods);
            
            // Inspect each pod
            for (V1Pod pod : podList.getItems()) {
                ProblematicPod issue = analyzePod(pod);
                if (issue != null) {
                    problematicPods.add(issue);
                    log.debug("Detected problematic pod: {} in namespace: {}",
                        issue.getName(), issue.getNamespace());
                }
            }
            
            int healthyPods = totalPods - problematicPods.size();
            boolean healthy = problematicPods.isEmpty();
            
            log.info("Pod inspection complete: {} total, {} healthy, {} problematic",
                totalPods, healthyPods, problematicPods.size());
            
            return PodInspectionResult.builder()
                .healthy(healthy)
                .problematicPods(problematicPods)
                .totalPods(totalPods)
                .healthyPods(healthyPods)
                .problematicPodsCount(problematicPods.size())
                .build();
                
        } catch (ApiException e) {
            log.error("Failed to inspect pods: {} - {}", e.getCode(), e.getResponseBody(), e);
            throw new RuntimeException("Failed to inspect pods: " + e.getMessage(), e);
        }
    }

    /**
     * Analyzes a single pod for issues.
     *
     * @param pod the pod to analyze
     * @return ProblematicPod if issues found, null otherwise
     */
    private ProblematicPod analyzePod(V1Pod pod) {
        String podName = pod.getMetadata().getName();
        String namespace = pod.getMetadata().getNamespace();
        String phase = pod.getStatus().getPhase();
        
        // Check pod phase
        if ("Failed".equals(phase) || "Unknown".equals(phase)) {
            return buildProblematicPod(pod, phase, "Pod in " + phase + " state",
                pod.getStatus().getMessage());
        }
        
        // Check if pod is pending for too long
        if ("Pending".equals(phase)) {
            String reason = pod.getStatus().getReason();
            return buildProblematicPod(pod, "Pending",
                reason != null ? reason : "PodPending",
                pod.getStatus().getMessage());
        }
        
        // Check container statuses
        if (pod.getStatus().getContainerStatuses() != null) {
            for (V1ContainerStatus containerStatus : pod.getStatus().getContainerStatuses()) {
                
                // Check for CrashLoopBackOff
                if (containerStatus.getState() != null &&
                    containerStatus.getState().getWaiting() != null) {
                    
                    String reason = containerStatus.getState().getWaiting().getReason();
                    
                    if ("CrashLoopBackOff".equals(reason) ||
                        "ImagePullBackOff".equals(reason) ||
                        "ErrImagePull".equals(reason) ||
                        "CreateContainerConfigError".equals(reason) ||
                        "InvalidImageName".equals(reason)) {
                        
                        return buildProblematicPod(pod, reason, reason,
                            containerStatus.getState().getWaiting().getMessage());
                    }
                }
                
                // Check for OOMKilled
                if (containerStatus.getLastState() != null &&
                    containerStatus.getLastState().getTerminated() != null) {
                    
                    String reason = containerStatus.getLastState().getTerminated().getReason();
                    if ("OOMKilled".equals(reason) || "Error".equals(reason)) {
                        int restartCount = containerStatus.getRestartCount();
                        return buildProblematicPod(pod, reason, reason,
                            "Container terminated: " + reason + ", restarts: " + restartCount);
                    }
                }
                
                // Check for high restart count
                if (containerStatus.getRestartCount() > 5) {
                    return buildProblematicPod(pod, "HighRestartCount",
                        "FrequentRestarts",
                        "Container has restarted " + containerStatus.getRestartCount() + " times");
                }
            }
        }
        
        // Check init container statuses
        if (pod.getStatus().getInitContainerStatuses() != null) {
            for (V1ContainerStatus initStatus : pod.getStatus().getInitContainerStatuses()) {
                if (initStatus.getState() != null &&
                    initStatus.getState().getWaiting() != null) {
                    
                    String reason = initStatus.getState().getWaiting().getReason();
                    if (reason != null && !reason.equals("PodInitializing")) {
                        return buildProblematicPod(pod, "InitContainerFailed", reason,
                            "Init container issue: " + initStatus.getState().getWaiting().getMessage());
                    }
                }
            }
        }
        
        return null; // Pod is healthy
    }

    /**
     * Builds a ProblematicPod DTO from pod information.
     */
    private ProblematicPod buildProblematicPod(V1Pod pod, String status,
                                                String reason, String message) {
        int restartCount = 0;
        if (pod.getStatus().getContainerStatuses() != null &&
            !pod.getStatus().getContainerStatuses().isEmpty()) {
            restartCount = pod.getStatus().getContainerStatuses().get(0).getRestartCount();
        }
        
        return ProblematicPod.builder()
            .name(pod.getMetadata().getName())
            .namespace(pod.getMetadata().getNamespace())
            .status(status)
            .reason(reason != null ? reason : "Unknown")
            .restartCount(restartCount)
            .message(message != null ? message : "No additional information")
            .build();
    }
}
