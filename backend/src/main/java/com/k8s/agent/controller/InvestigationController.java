package com.k8s.agent.controller;

import com.k8s.agent.dto.ai.InvestigationResponse;
import com.k8s.agent.dto.common.ApiResponse;
import com.k8s.agent.dto.investigation.InvestigationResult;
import com.k8s.agent.service.ai.AIService;
import com.k8s.agent.service.investigation.InvestigationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Kubernetes cluster investigation endpoints.
 * Provides both raw investigation data and AI-powered diagnosis.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/investigate")
@RequiredArgsConstructor
public class InvestigationController {

    private final InvestigationService investigationService;
    private final AIService aiService;

    /**
     * Performs a comprehensive investigation with AI-powered diagnosis.
     * This is the main endpoint that combines investigation and AI analysis.
     *
     * @return Investigation results with AI diagnosis
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InvestigationResponse>> investigate() {
        log.info("Received request to investigate Kubernetes cluster with AI diagnosis");
        
        try {
            // Step 1: Perform investigation
            InvestigationResult investigation = investigationService.investigate();
            
            // Step 2: Get AI diagnosis
            InvestigationResponse response = aiService.diagnose(investigation);
            
            String message = buildResponseMessage(response);
            
            log.info("Investigation with AI diagnosis completed. Status: {}", response.getStatus());
            
            return ResponseEntity.ok(
                ApiResponse.<InvestigationResponse>builder()
                    .success(true)
                    .message(message)
                    .data(response)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Investigation with AI diagnosis failed: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<InvestigationResponse>builder()
                    .success(false)
                    .message("Investigation failed: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * Performs investigation without AI diagnosis (raw data only).
     * Useful for debugging or when AI is not needed.
     *
     * @return Raw investigation results
     */
    @PostMapping("/raw")
    public ResponseEntity<ApiResponse<InvestigationResult>> investigateRaw() {
        log.info("Received request for raw investigation (no AI)");
        
        try {
            InvestigationResult result = investigationService.investigate();
            
            String message = result.isClusterHealthy()
                ? "Cluster investigation complete - No issues detected"
                : "Cluster investigation complete - Issues detected";
            
            log.info("Raw investigation completed. Cluster healthy: {}", result.isClusterHealthy());
            
            return ResponseEntity.ok(
                ApiResponse.<InvestigationResult>builder()
                    .success(true)
                    .message(message)
                    .data(result)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Raw investigation failed: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<InvestigationResult>builder()
                    .success(false)
                    .message("Raw investigation failed: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * Performs a quick health check of the cluster.
     * Only checks pods and events for faster response.
     * 
     * @return Quick health check results
     */
    @GetMapping("/quick")
    public ResponseEntity<ApiResponse<InvestigationResult>> quickHealthCheck() {
        log.info("Received request for quick health check");
        
        try {
            InvestigationResult result = investigationService.quickHealthCheck();
            
            String message = result.isClusterHealthy() 
                ? "Quick health check complete - Cluster healthy"
                : "Quick health check complete - Issues detected";
            
            log.info("Quick health check completed. Cluster healthy: {}", 
                result.isClusterHealthy());
            
            return ResponseEntity.ok(
                ApiResponse.<InvestigationResult>builder()
                    .success(true)
                    .message(message)
                    .data(result)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Quick health check failed: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<InvestigationResult>builder()
                    .success(false)
                    .message("Quick health check failed: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * Health check endpoint for the investigation service.
     *
     * @return Service health status including AI availability
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        boolean aiAvailable = aiService.isAIAvailable();
        String status = aiAvailable
            ? "Investigation service is healthy (AI enabled)"
            : "Investigation service is healthy (AI disabled)";
        
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .success(true)
                .message(status)
                .data("OK")
                .build()
        );
    }

    /**
     * Builds a response message based on investigation results.
     *
     * @param response Investigation response
     * @return Human-readable message
     */
    private String buildResponseMessage(InvestigationResponse response) {
        StringBuilder message = new StringBuilder();
        
        // Investigation status
        if (response.getInvestigation().isClusterHealthy()) {
            message.append("Cluster appears healthy. ");
        } else {
            message.append("Issues detected in cluster. ");
        }
        
        // AI diagnosis status
        if ("completed_with_ai".equals(response.getStatus())) {
            String confidenceLevel = aiService.getConfidenceLevel(response.getDiagnosis());
            message.append(String.format("AI diagnosis completed (Confidence: %s - %d%%). ",
                    confidenceLevel, response.getDiagnosis().getConfidence()));
        } else if ("completed_without_ai".equals(response.getStatus())) {
            message.append("AI diagnosis not available. ");
        } else if ("completed_with_errors".equals(response.getStatus())) {
            message.append("AI diagnosis failed, fallback diagnosis provided. ");
        }
        
        return message.toString().trim();
    }
}

// Made with Bob
