package com.k8s.agent.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.agent.dto.investigation.InvestigationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for building structured prompts for the LLM.
 * Creates both system prompts (defining the AI's role) and user prompts (containing investigation data).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptBuilderService {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Builds the system prompt that defines the AI's role as a Senior Kubernetes SRE.
     * 
     * @return System prompt string
     */
    public String buildSystemPrompt() {
        return """
            You are a Senior Kubernetes Site Reliability Engineer (SRE) with 10+ years of experience.
            
            Your expertise includes:
            - Kubernetes architecture and troubleshooting
            - Container orchestration and debugging
            - Root cause analysis of production incidents
            - DevOps best practices and prevention strategies
            
            Your task is to analyze Kubernetes investigation data and provide:
            1. Root cause identification
            2. Clear explanation of the issue
            3. Actionable fix recommendations
            4. Specific kubectl commands to resolve the issue
            5. Prevention strategies to avoid future occurrences
            6. Confidence score (0-100) for your diagnosis
            
            Response Format (JSON):
            {
              "root_cause": "Primary issue identified",
              "explanation": "Detailed explanation of why this is the root cause",
              "fix": "High-level fix recommendation",
              "kubectl_commands": ["command1", "command2"],
              "prevention": "Strategies to prevent this issue",
              "confidence": 85
            }
            
            Guidelines:
            - Be precise and technical
            - Prioritize the most critical issues
            - Provide actionable, tested solutions
            - Consider security and best practices
            - If multiple issues exist, focus on the primary root cause
            - Base confidence on evidence quality and clarity
            """;
    }
    
    /**
     * Builds the user prompt containing investigation data.
     * 
     * @param investigation Investigation result from Kubernetes cluster
     * @return User prompt string with structured investigation data
     */
    public String buildUserPrompt(InvestigationResult investigation) {
        try {
            String investigationJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(investigation);
            
            return String.format("""
                Analyze the following Kubernetes investigation data and provide a diagnosis:
                
                Investigation Data:
                %s
                
                Please analyze this data and provide:
                1. The root cause of any issues found
                2. A detailed explanation
                3. Recommended fixes
                4. Specific kubectl commands to execute
                5. Prevention strategies
                6. Your confidence level (0-100)
                
                Respond ONLY with valid JSON matching the specified format.
                """, investigationJson);
                
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize investigation data to JSON", e);
            return buildFallbackUserPrompt(investigation);
        }
    }
    
    /**
     * Builds a fallback user prompt when JSON serialization fails.
     * 
     * @param investigation Investigation result
     * @return Simplified user prompt
     */
    private String buildFallbackUserPrompt(InvestigationResult investigation) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following Kubernetes investigation:\n\n");
        
        // Add summary information
        prompt.append("Summary:\n");
        prompt.append(String.format("- Cluster Healthy: %s\n", investigation.isClusterHealthy()));
        
        if (investigation.getPodInspection() != null) {
            prompt.append(String.format("- Total Pods: %d\n", investigation.getPodInspection().getTotalPods()));
            prompt.append(String.format("- Problematic Pods: %d\n", investigation.getPodInspection().getProblematicPodsCount()));
            prompt.append(String.format("- Healthy Pods: %d\n", investigation.getPodInspection().getHealthyPods()));
        }
        
        if (investigation.getDeploymentInspection() != null) {
            prompt.append(String.format("- Deployments: %d\n", investigation.getDeploymentInspection().getTotalDeployments()));
        }
        
        if (investigation.getNetworkInspection() != null) {
            prompt.append(String.format("- Services: %d\n", investigation.getNetworkInspection().getTotalServices()));
        }
        prompt.append("\n");
        
        // Add problematic pods
        if (investigation.getPodInspection() != null &&
            investigation.getPodInspection().getProblematicPodsCount() > 0) {
            prompt.append("Problematic Pods:\n");
            investigation.getPodInspection().getProblematicPods().forEach(pod -> {
                prompt.append(String.format("- %s (Namespace: %s, Status: %s, Reason: %s)\n",
                        pod.getName(), pod.getNamespace(), pod.getStatus(), pod.getReason()));
            });
            prompt.append("\n");
        }
        
        // Add critical events
        if (investigation.getEventsAnalysis() != null &&
            investigation.getEventsAnalysis().getCriticalEventsCount() > 0) {
            prompt.append("Critical Events:\n");
            investigation.getEventsAnalysis().getCriticalEvents().forEach(event -> {
                prompt.append(String.format("- %s: %s\n", event.getReason(), event.getMessage()));
            });
            prompt.append("\n");
        }
        
        prompt.append("Please provide a diagnosis in JSON format as specified.");
        
        return prompt.toString();
    }
}

// Made with Bob
