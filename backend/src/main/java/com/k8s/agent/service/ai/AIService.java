package com.k8s.agent.service.ai;

import com.k8s.agent.dto.ai.DiagnosisResult;
import com.k8s.agent.dto.ai.InvestigationResponse;
import com.k8s.agent.dto.ai.OpenRouterResponse;
import com.k8s.agent.dto.investigation.InvestigationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main AI service that orchestrates all AI reasoning components.
 * Coordinates prompt building, LLM communication, response parsing, and enhancement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    
    private final PromptBuilderService promptBuilderService;
    private final OpenRouterClientService openRouterClientService;
    private final RootCauseAnalyzerService rootCauseAnalyzerService;
    private final FixRecommendationService fixRecommendationService;
    private final ConfidenceEngineService confidenceEngineService;
    
    /**
     * Performs complete AI-powered diagnosis of investigation results.
     * 
     * @param investigation Investigation result from Kubernetes cluster
     * @return Complete investigation response with AI diagnosis
     */
    public InvestigationResponse diagnose(InvestigationResult investigation) {
        log.info("Starting AI diagnosis for investigation");
        
        DiagnosisResult diagnosis;
        String status;
        
        try {
            // Check if OpenRouter is configured
            if (!openRouterClientService.isConfigured()) {
                log.warn("OpenRouter is not configured, returning investigation without AI diagnosis");
                diagnosis = createUnconfiguredDiagnosis();
                status = "completed_without_ai";
            } else {
                // Perform AI diagnosis
                diagnosis = performAIDiagnosis(investigation);
                status = "completed_with_ai";
            }
            
        } catch (Exception e) {
            log.error("AI diagnosis failed, returning fallback diagnosis", e);
            diagnosis = rootCauseAnalyzerService.createFallbackDiagnosis(e.getMessage());
            status = "completed_with_errors";
        }
        
        // Build complete response
        return InvestigationResponse.builder()
                .investigation(investigation)
                .diagnosis(diagnosis)
                .status(status)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
    
    /**
     * Performs the complete AI diagnosis workflow.
     * 
     * @param investigation Investigation result
     * @return AI-powered diagnosis
     */
    private DiagnosisResult performAIDiagnosis(InvestigationResult investigation) {
        log.info("Performing AI diagnosis workflow");
        
        // Step 1: Build prompts
        log.debug("Building prompts for LLM");
        String systemPrompt = promptBuilderService.buildSystemPrompt();
        String userPrompt = promptBuilderService.buildUserPrompt(investigation);
        
        // Step 2: Call OpenRouter API
        log.debug("Calling OpenRouter API");
        OpenRouterResponse response = openRouterClientService.sendChatCompletion(systemPrompt, userPrompt);
        
        // Step 3: Parse response
        log.debug("Parsing AI response");
        DiagnosisResult diagnosis = rootCauseAnalyzerService.parseResponse(response);
        
        // Step 4: Enhance recommendations
        log.debug("Enhancing fix recommendations");
        diagnosis = fixRecommendationService.enhanceRecommendations(diagnosis);
        
        // Step 5: Adjust confidence
        log.debug("Adjusting confidence score");
        diagnosis = confidenceEngineService.adjustConfidence(diagnosis, investigation);
        
        // Step 6: Validate
        if (!fixRecommendationService.validateRecommendations(diagnosis)) {
            log.warn("Diagnosis validation failed, but returning anyway with warning");
            diagnosis.setWarning("Diagnosis validation failed. Please review recommendations carefully.");
        }
        
        log.info("AI diagnosis completed successfully: root_cause={}, confidence={}", 
                diagnosis.getRootCause(), diagnosis.getConfidence());
        
        return diagnosis;
    }
    
    /**
     * Creates a diagnosis result when OpenRouter is not configured.
     * 
     * @return Unconfigured diagnosis
     */
    private DiagnosisResult createUnconfiguredDiagnosis() {
        return DiagnosisResult.builder()
                .rootCause("AI Analysis Not Available")
                .explanation("The AI reasoning engine is not configured. Please set the OPENROUTER_API_KEY environment variable to enable AI-powered diagnosis.")
                .fix("Review the investigation data manually to identify issues.")
                .kubectlCommands(java.util.Collections.emptyList())
                .prevention("Configure OpenRouter API key to enable AI diagnosis.")
                .confidence(0)
                .warning("OpenRouter API is not configured. AI diagnosis is unavailable.")
                .build();
    }
    
    /**
     * Checks if AI diagnosis is available.
     * 
     * @return true if AI is configured and available, false otherwise
     */
    public boolean isAIAvailable() {
        return openRouterClientService.isConfigured();
    }
    
    /**
     * Gets the confidence level description for a diagnosis.
     * 
     * @param diagnosis Diagnosis result
     * @return Confidence level description
     */
    public String getConfidenceLevel(DiagnosisResult diagnosis) {
        if (diagnosis == null || diagnosis.getConfidence() == null) {
            return "Unknown";
        }
        return confidenceEngineService.getConfidenceLevel(diagnosis.getConfidence());
    }
}

// Made with Bob
