package com.k8s.agent.dto.investigation;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a service with networking issues.
 */
@Data
@Builder
public class ServiceIssue {
    
    /**
     * Name of the service
     */
    private String serviceName;
    
    /**
     * Namespace of the service
     */
    private String namespace;
    
    /**
     * Service type (ClusterIP, NodePort, LoadBalancer)
     */
    private String serviceType;
    
    /**
     * Issue type (e.g., NoEndpoints, SelectorMismatch)
     */
    private String issueType;
    
    /**
     * Description of the issue
     */
    private String description;
    
    /**
     * Service selector labels (as Map converted to string or null)
     */
    private java.util.Map<String, String> selector;
}
