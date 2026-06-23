package com.k8s.agent.service.investigation;

import com.k8s.agent.dto.investigation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Orchestrator service that coordinates all Kubernetes investigation components.
 * Executes investigation steps sequentially and aggregates results.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvestigationService {

    private final PodInspectorService podInspectorService;
    private final LogsCollectorService logsCollectorService;
    private final EventsAnalyzerService eventsAnalyzerService;
    private final DeploymentInspectorService deploymentInspectorService;
    private final NetworkInspectorService networkInspectorService;

    /**
     * Performs a comprehensive investigation of the Kubernetes cluster.
     * Executes all investigation steps and returns aggregated results.
     *
     * @return InvestigationResult containing all investigation findings
     */
    public InvestigationResult investigate() {
        log.info("Starting comprehensive Kubernetes cluster investigation");
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // Step 1: Inspect Pods
            log.info("Step 1/5: Inspecting pods...");
            PodInspectionResult podResult = podInspectorService.inspectPods();
            log.info("Pod inspection complete: {} problematic pods found", 
                podResult.getProblematicPodsCount());
            
            // Step 2: Collect Logs from problematic pods
            log.info("Step 2/5: Collecting logs from problematic pods...");
            LogsCollectionResult logsResult = logsCollectorService.collectLogs(
                podResult.getProblematicPods()
            );
            log.info("Logs collection complete: {} pods with error logs", 
                logsResult.getPodsWithErrorLogs());
            
            // Step 3: Analyze Events
            log.info("Step 3/5: Analyzing Kubernetes events...");
            EventsAnalysisResult eventsResult = eventsAnalyzerService.analyzeEvents();
            log.info("Events analysis complete: {} critical events found", 
                eventsResult.getCriticalEventsCount());
            
            // Step 4: Inspect Deployments
            log.info("Step 4/5: Inspecting deployments...");
            DeploymentInspectionResult deploymentResult = 
                deploymentInspectorService.inspectDeployments();
            log.info("Deployment inspection complete: {} problematic deployments found", 
                deploymentResult.getProblematicDeploymentsCount());
            
            // Step 5: Inspect Network
            log.info("Step 5/5: Inspecting network and services...");
            NetworkInspectionResult networkResult = networkInspectorService.inspectNetwork();
            log.info("Network inspection complete: {} services with issues", 
                networkResult.getServicesWithIssues());
            
            // Determine overall cluster health
            boolean clusterHealthy = podResult.isHealthy() 
                && eventsResult.isHealthy() 
                && deploymentResult.isHealthy() 
                && networkResult.isHealthy();
            
            LocalDateTime endTime = LocalDateTime.now();
            
            InvestigationResult result = InvestigationResult.builder()
                .timestamp(endTime)
                .clusterHealthy(clusterHealthy)
                .podInspection(podResult)
                .logsCollection(logsResult)
                .eventsAnalysis(eventsResult)
                .deploymentInspection(deploymentResult)
                .networkInspection(networkResult)
                .investigationDurationSeconds(
                    java.time.Duration.between(startTime, endTime).getSeconds()
                )
                .build();
            
            log.info("Investigation complete. Cluster healthy: {}. Duration: {}s", 
                clusterHealthy, result.getInvestigationDurationSeconds());
            
            return result;
            
        } catch (Exception e) {
            log.error("Investigation failed with error: {}", e.getMessage(), e);
            throw new RuntimeException("Investigation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Performs a quick health check of the cluster.
     * Only checks pods and events for faster response.
     *
     * @return InvestigationResult with limited scope
     */
    public InvestigationResult quickHealthCheck() {
        log.info("Starting quick health check");
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // Quick check: only pods and events
            PodInspectionResult podResult = podInspectorService.inspectPods();
            EventsAnalysisResult eventsResult = eventsAnalyzerService.analyzeEvents();
            
            boolean clusterHealthy = podResult.isHealthy() && eventsResult.isHealthy();
            LocalDateTime endTime = LocalDateTime.now();
            
            InvestigationResult result = InvestigationResult.builder()
                .timestamp(endTime)
                .clusterHealthy(clusterHealthy)
                .podInspection(podResult)
                .eventsAnalysis(eventsResult)
                .investigationDurationSeconds(
                    java.time.Duration.between(startTime, endTime).getSeconds()
                )
                .build();
            
            log.info("Quick health check complete. Cluster healthy: {}. Duration: {}s", 
                clusterHealthy, result.getInvestigationDurationSeconds());
            
            return result;
            
        } catch (Exception e) {
            log.error("Quick health check failed: {}", e.getMessage(), e);
            throw new RuntimeException("Quick health check failed: " + e.getMessage(), e);
        }
    }
}

// Made with Bob
