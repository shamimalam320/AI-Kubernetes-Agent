package com.k8s.agent.dto.ai;

import com.k8s.agent.dto.investigation.InvestigationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the complete investigation response including both
 * raw investigation data and AI-powered diagnosis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationResponse {
    
    /**
     * Raw investigation data collected from Kubernetes cluster
     */
    private InvestigationResult investigation;
    
    /**
     * AI-powered diagnosis and recommendations
     */
    private DiagnosisResult diagnosis;
    
    /**
     * Overall status of the investigation
     */
    private String status;
    
    /**
     * Timestamp when the investigation was completed
     */
    private String timestamp;
}

// Made with Bob
