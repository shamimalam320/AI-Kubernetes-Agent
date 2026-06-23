package com.k8s.agent.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.agent.dto.ai.DiagnosisResult;
import com.k8s.agent.dto.ai.OpenRouterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service responsible for parsing and validating AI responses.
 * Extracts diagnosis information from LLM output and validates the structure.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RootCauseAnalyzerService {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Parses the OpenRouter response and extracts the diagnosis result.
     * 
     * @param response OpenRouter API response
     * @return Parsed and validated diagnosis result
     * @throws RuntimeException if parsing fails
     */
    public DiagnosisResult parseResponse(OpenRouterResponse response) {
        try {
            // Extract message content from response
            if (response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new RuntimeException("No choices in OpenRouter response");
            }
            
            String content = response.getChoices().get(0).getMessage().getContent();
            if (content == null || content.isBlank()) {
                throw new RuntimeException("Empty content in OpenRouter response");
            }
            
            log.debug("Parsing AI response content: {}", content);
            
            // Extract JSON from response (handle markdown code blocks)
            String jsonContent = extractJson(content);
            
            // Parse JSON to DiagnosisResult
            DiagnosisResult diagnosis = objectMapper.readValue(jsonContent, DiagnosisResult.class);
            
            // Validate the diagnosis
            validateDiagnosis(diagnosis);
            
            log.info("Successfully parsed diagnosis: root_cause={}, confidence={}", 
                    diagnosis.getRootCause(), diagnosis.getConfidence());
            
            return diagnosis;
            
        } catch (Exception e) {
            log.error("Failed to parse OpenRouter response", e);
            throw new RuntimeException("Failed to parse AI diagnosis: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extracts JSON content from the response, handling markdown code blocks.
     * 
     * @param content Raw response content
     * @return Extracted JSON string
     */
    private String extractJson(String content) {
        // Try to extract JSON from markdown code block
        Pattern codeBlockPattern = Pattern.compile("```(?:json)?\\s*\\n?(.+?)```", Pattern.DOTALL);
        Matcher matcher = codeBlockPattern.matcher(content);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // Try to find JSON object directly
        Pattern jsonPattern = Pattern.compile("\\{.+\\}", Pattern.DOTALL);
        matcher = jsonPattern.matcher(content);
        
        if (matcher.find()) {
            return matcher.group(0).trim();
        }
        
        // Return content as-is if no patterns match
        return content.trim();
    }
    
    /**
     * Validates that the diagnosis contains all required fields.
     * 
     * @param diagnosis Diagnosis result to validate
     * @throws RuntimeException if validation fails
     */
    private void validateDiagnosis(DiagnosisResult diagnosis) {
        if (diagnosis.getRootCause() == null || diagnosis.getRootCause().isBlank()) {
            throw new RuntimeException("Diagnosis missing root_cause");
        }
        
        if (diagnosis.getExplanation() == null || diagnosis.getExplanation().isBlank()) {
            throw new RuntimeException("Diagnosis missing explanation");
        }
        
        if (diagnosis.getFix() == null || diagnosis.getFix().isBlank()) {
            throw new RuntimeException("Diagnosis missing fix");
        }
        
        if (diagnosis.getKubectlCommands() == null) {
            diagnosis.setKubectlCommands(new ArrayList<>());
        }
        
        if (diagnosis.getPrevention() == null || diagnosis.getPrevention().isBlank()) {
            diagnosis.setPrevention("No prevention strategy provided");
        }
        
        if (diagnosis.getConfidence() == null) {
            log.warn("Diagnosis missing confidence score, defaulting to 50");
            diagnosis.setConfidence(50);
        } else if (diagnosis.getConfidence() < 0 || diagnosis.getConfidence() > 100) {
            log.warn("Invalid confidence score: {}, clamping to 0-100 range", diagnosis.getConfidence());
            diagnosis.setConfidence(Math.max(0, Math.min(100, diagnosis.getConfidence())));
        }
    }
    
    /**
     * Creates a fallback diagnosis when AI analysis fails.
     * 
     * @param errorMessage Error message to include
     * @return Fallback diagnosis result
     */
    public DiagnosisResult createFallbackDiagnosis(String errorMessage) {
        return DiagnosisResult.builder()
                .rootCause("AI Analysis Unavailable")
                .explanation("The AI reasoning engine encountered an error and could not complete the analysis.")
                .fix("Please review the investigation data manually or try again later.")
                .kubectlCommands(new ArrayList<>())
                .prevention("Ensure OpenRouter API is properly configured and accessible.")
                .confidence(0)
                .warning("AI diagnosis failed: " + errorMessage)
                .build();
    }
}

// Made with Bob
