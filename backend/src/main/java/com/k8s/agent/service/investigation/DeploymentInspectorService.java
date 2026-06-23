package com.k8s.agent.service.investigation;

import com.k8s.agent.dto.investigation.DeploymentInspectionResult;
import com.k8s.agent.dto.investigation.ProblematicDeployment;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentCondition;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1DeploymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for inspecting Kubernetes deployments.
 * Detects unhealthy deployments and rollout issues.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentInspectorService {

    private final AppsV1Api appsV1Api;

    /**
     * Inspects all deployments in the cluster.
     *
     * @return DeploymentInspectionResult containing deployment health status
     */
    public DeploymentInspectionResult inspectDeployments() {
        log.info("Starting deployment inspection across all namespaces");
        
        List<ProblematicDeployment> problematicDeployments = new ArrayList<>();
        int totalDeployments = 0;
        
        try {
            // Get all deployments across all namespaces
            V1DeploymentList deploymentList = appsV1Api.listDeploymentForAllNamespaces(
                null, null, null, null, null, null, null, null, null, null, null
            );
            
            totalDeployments = deploymentList.getItems().size();
            log.debug("Found {} total deployments in cluster", totalDeployments);
            
            // Inspect each deployment
            for (V1Deployment deployment : deploymentList.getItems()) {
                ProblematicDeployment issue = analyzeDeployment(deployment);
                if (issue != null) {
                    problematicDeployments.add(issue);
                    log.debug("Detected problematic deployment: {} in namespace: {}", 
                        issue.getName(), issue.getNamespace());
                }
            }
            
            int healthyDeployments = totalDeployments - problematicDeployments.size();
            boolean healthy = problematicDeployments.isEmpty();
            
            log.info("Deployment inspection complete: {} total, {} healthy, {} problematic", 
                totalDeployments, healthyDeployments, problematicDeployments.size());
            
            return DeploymentInspectionResult.builder()
                .healthy(healthy)
                .problematicDeployments(problematicDeployments)
                .totalDeployments(totalDeployments)
                .healthyDeployments(healthyDeployments)
                .problematicDeploymentsCount(problematicDeployments.size())
                .build();
                
        } catch (ApiException e) {
            log.error("Failed to inspect deployments: {} - {}", e.getCode(), e.getResponseBody(), e);
            throw new RuntimeException("Failed to inspect deployments: " + e.getMessage(), e);
        }
    }

    /**
     * Analyzes a single deployment for issues.
     *
     * @param deployment the deployment to analyze
     * @return ProblematicDeployment if issues found, null otherwise
     */
    private ProblematicDeployment analyzeDeployment(V1Deployment deployment) {
        String name = deployment.getMetadata().getName();
        String namespace = deployment.getMetadata().getNamespace();
        V1DeploymentStatus status = deployment.getStatus();
        
        if (status == null) {
            return null;
        }
        
        Integer desiredReplicas = deployment.getSpec().getReplicas();
        Integer availableReplicas = status.getAvailableReplicas();
        Integer unavailableReplicas = status.getUnavailableReplicas();
        
        // Default values if null
        int desired = desiredReplicas != null ? desiredReplicas : 0;
        int available = availableReplicas != null ? availableReplicas : 0;
        int unavailable = unavailableReplicas != null ? unavailableReplicas : 0;
        
        // Check if deployment has unavailable replicas
        if (unavailable > 0) {
            return buildProblematicDeployment(deployment, "UnavailableReplicas",
                "ReplicasUnavailable",
                String.format("%d/%d replicas unavailable", unavailable, desired));
        }
        
        // Check if available replicas don't match desired
        if (available < desired) {
            return buildProblematicDeployment(deployment, "InsufficientReplicas",
                "ScalingIssue",
                String.format("Only %d/%d replicas available", available, desired));
        }
        
        // Check deployment conditions
        if (status.getConditions() != null) {
            for (V1DeploymentCondition condition : status.getConditions()) {
                String type = condition.getType();
                String conditionStatus = condition.getStatus();
                
                // Check for failed conditions
                if ("Progressing".equals(type) && "False".equals(conditionStatus)) {
                    return buildProblematicDeployment(deployment, "ProgressDeadlineExceeded",
                        condition.getReason(),
                        condition.getMessage());
                }
                
                if ("Available".equals(type) && "False".equals(conditionStatus)) {
                    return buildProblematicDeployment(deployment, "NotAvailable",
                        condition.getReason(),
                        condition.getMessage());
                }
                
                if ("ReplicaFailure".equals(type) && "True".equals(conditionStatus)) {
                    return buildProblematicDeployment(deployment, "ReplicaFailure",
                        condition.getReason(),
                        condition.getMessage());
                }
            }
        }
        
        return null; // Deployment is healthy
    }

    /**
     * Builds a ProblematicDeployment DTO from deployment information.
     */
    private ProblematicDeployment buildProblematicDeployment(V1Deployment deployment,
                                                              String status,
                                                              String reason,
                                                              String message) {
        V1DeploymentStatus deploymentStatus = deployment.getStatus();
        
        Integer desiredReplicas = deployment.getSpec().getReplicas();
        Integer availableReplicas = deploymentStatus != null ? 
            deploymentStatus.getAvailableReplicas() : 0;
        Integer unavailableReplicas = deploymentStatus != null ? 
            deploymentStatus.getUnavailableReplicas() : 0;
        
        return ProblematicDeployment.builder()
            .name(deployment.getMetadata().getName())
            .namespace(deployment.getMetadata().getNamespace())
            .desiredReplicas(desiredReplicas != null ? desiredReplicas : 0)
            .availableReplicas(availableReplicas != null ? availableReplicas : 0)
            .unavailableReplicas(unavailableReplicas != null ? unavailableReplicas : 0)
            .status(status)
            .reason(reason != null ? reason : "Unknown")
            .message(message != null ? message : "No additional information")
            .build();
    }
}

// Made with Bob
