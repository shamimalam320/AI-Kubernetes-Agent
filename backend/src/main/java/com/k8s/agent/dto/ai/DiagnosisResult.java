package com.k8s.agent.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing the AI diagnosis result from the LLM.
 * Uses snake_case for JSON field names to match LLM output format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisResult {
    
    /**
     * The primary root cause of the Kubernetes issue
     */
    @JsonProperty("root_cause")
    private String rootCause;
    
    /**
     * Detailed explanation of why this is the root cause
     */
    @JsonProperty("explanation")
    private String explanation;
    
    /**
     * High-level fix recommendation
     */
    @JsonProperty("fix")
    private String fix;
    
    /**
     * List of specific kubectl commands to execute
     */
    @JsonProperty("kubectl_commands")
    private List<String> kubectlCommands;
    
    /**
     * Prevention strategies to avoid this issue in the future
     */
    @JsonProperty("prevention")
    private String prevention;
    
    /**
     * Confidence score (0-100) indicating how certain the AI is about the diagnosis
     */
    @JsonProperty("confidence")
    private Integer confidence;
    
    /**
     * Optional warning message if AI analysis failed or has limitations
     */
    @JsonProperty("warning")
    private String warning;
}

// Made with Bob
