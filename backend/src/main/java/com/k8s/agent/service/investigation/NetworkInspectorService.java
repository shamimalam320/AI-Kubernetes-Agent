package com.k8s.agent.service.investigation;

import com.k8s.agent.dto.investigation.NetworkInspectionResult;
import com.k8s.agent.dto.investigation.ServiceIssue;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for inspecting Kubernetes networking and services.
 * Detects service selector mismatches and endpoint issues.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkInspectorService {

    private final CoreV1Api coreV1Api;

    /**
     * Inspects all services and their networking configuration.
     *
     * @return NetworkInspectionResult containing service health status
     */
    public NetworkInspectionResult inspectNetwork() {
        log.info("Starting network inspection across all namespaces");
        
        List<ServiceIssue> serviceIssues = new ArrayList<>();
        int totalServices = 0;
        
        try {
            // Get all services across all namespaces
            V1ServiceList serviceList = coreV1Api.listServiceForAllNamespaces(
                null, null, null, null, null, null, null, null, null, null, null
            );
            
            totalServices = serviceList.getItems().size();
            log.debug("Found {} total services in cluster", totalServices);
            
            // Inspect each service
            for (V1Service service : serviceList.getItems()) {
                ServiceIssue issue = analyzeService(service);
                if (issue != null) {
                    serviceIssues.add(issue);
                    log.debug("Detected service issue: {} in namespace: {}", 
                        issue.getServiceName(), issue.getNamespace());
                }
            }
            
            int healthyServices = totalServices - serviceIssues.size();
            boolean healthy = serviceIssues.isEmpty();
            
            log.info("Network inspection complete: {} total services, {} healthy, {} with issues", 
                totalServices, healthyServices, serviceIssues.size());
            
            return NetworkInspectionResult.builder()
                .healthy(healthy)
                .serviceIssues(serviceIssues)
                .totalServices(totalServices)
                .healthyServices(healthyServices)
                .servicesWithIssues(serviceIssues.size())
                .build();
                
        } catch (ApiException e) {
            log.error("Failed to inspect network: {} - {}", e.getCode(), e.getResponseBody(), e);
            throw new RuntimeException("Failed to inspect network: " + e.getMessage(), e);
        }
    }

    /**
     * Analyzes a single service for networking issues.
     *
     * @param service the service to analyze
     * @return ServiceIssue if problems found, null otherwise
     */
    private ServiceIssue analyzeService(V1Service service) {
        String serviceName = service.getMetadata().getName();
        String namespace = service.getMetadata().getNamespace();
        
        // Skip system services
        if ("kube-system".equals(namespace) || "kube-public".equals(namespace)) {
            return null;
        }
        
        V1ServiceSpec spec = service.getSpec();
        if (spec == null) {
            return null;
        }
        
        // Check if service has selectors
        Map<String, String> selectors = spec.getSelector();
        if (selectors == null || selectors.isEmpty()) {
            // Services without selectors might be external or headless
            String type = spec.getType();
            if (!"ExternalName".equals(type)) {
                return buildServiceIssue(service, "NoSelector",
                    "Service has no pod selector defined");
            }
            return null;
        }
        
        // Check if service has endpoints
        try {
            V1Endpoints endpoints = coreV1Api.readNamespacedEndpoints(serviceName, namespace, null);
            
            if (endpoints.getSubsets() == null || endpoints.getSubsets().isEmpty()) {
                return buildServiceIssue(service, "NoEndpoints",
                    "Service has no endpoints - no pods match the selector");
            }
            
            // Check if endpoints have ready addresses
            boolean hasReadyAddresses = false;
            for (V1EndpointSubset subset : endpoints.getSubsets()) {
                if (subset.getAddresses() != null && !subset.getAddresses().isEmpty()) {
                    hasReadyAddresses = true;
                    break;
                }
            }
            
            if (!hasReadyAddresses) {
                return buildServiceIssue(service, "NoReadyEndpoints",
                    "Service has endpoints but none are ready");
            }
            
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return buildServiceIssue(service, "EndpointsNotFound",
                    "Endpoints object not found for service");
            }
            log.warn("Failed to check endpoints for service {}/{}: {}", 
                namespace, serviceName, e.getMessage());
        }
        
        // Check for port configuration issues
        List<V1ServicePort> ports = spec.getPorts();
        if (ports == null || ports.isEmpty()) {
            return buildServiceIssue(service, "NoPorts",
                "Service has no ports defined");
        }
        
        // Check for duplicate port names
        List<String> portNames = new ArrayList<>();
        for (V1ServicePort port : ports) {
            String portName = port.getName();
            if (portName != null && !portName.isEmpty()) {
                if (portNames.contains(portName)) {
                    return buildServiceIssue(service, "DuplicatePortName",
                        "Service has duplicate port name: " + portName);
                }
                portNames.add(portName);
            }
        }
        
        return null; // Service is healthy
    }

    /**
     * Builds a ServiceIssue DTO from service information.
     */
    private ServiceIssue buildServiceIssue(V1Service service, String issueType, String description) {
        V1ServiceSpec spec = service.getSpec();
        
        return ServiceIssue.builder()
            .serviceName(service.getMetadata().getName())
            .namespace(service.getMetadata().getNamespace())
            .serviceType(spec != null ? spec.getType() : "Unknown")
            .issueType(issueType)
            .description(description)
            .selector(spec != null ? spec.getSelector() : null)
            .build();
    }
}

// Made with Bob
