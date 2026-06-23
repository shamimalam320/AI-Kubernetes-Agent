package com.k8s.agent.controller;

import com.k8s.agent.dto.common.ApiResponse;
import com.k8s.agent.dto.investigation.InvestigationResult;
import com.k8s.agent.service.investigation.InvestigationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Kubernetes cluster investigation endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/investigate")
@RequiredArgsConstructor
public class InvestigationController {

    private final InvestigationService investigationService;

    /**
     * Performs a comprehensive investigation of the Kubernetes cluster.
     * 
     * @return Investigation results with all findings
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InvestigationResult>> investigate() {
        log.info("Received request to investigate Kubernetes cluster");
        
        try {
            InvestigationResult result = investigationService.investigate();
            
            String message = result.isClusterHealthy() 
                ? "Cluster investigation complete - No issues detected"
                : "Cluster investigation complete - Issues detected";
            
            log.info("Investigation completed successfully. Cluster healthy: {}", 
                result.isClusterHealthy());
            
            return ResponseEntity.ok(
                ApiResponse.<InvestigationResult>builder()
                    .success(true)
                    .message(message)
                    .data(result)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Investigation failed: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<InvestigationResult>builder()
                    .success(false)
                    .message("Investigation failed: " + e.getMessage())
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
     * @return Service health status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .success(true)
                .message("Investigation service is healthy")
                .data("OK")
                .build()
        );
    }
}

// Made with Bob
