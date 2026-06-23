package com.k8s.agent.service.ai;

import com.k8s.agent.dto.ai.DiagnosisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for enhancing and validating fix recommendations.
 * Ensures kubectl commands are safe and properly formatted.
 */
@Slf4j
@Service
public class FixRecommendationService {
    
    /**
     * Enhances the diagnosis with improved kubectl commands and recommendations.
     * 
     * @param diagnosis Original diagnosis from AI
     * @return Enhanced diagnosis with validated commands
     */
    public DiagnosisResult enhanceRecommendations(DiagnosisResult diagnosis) {
        if (diagnosis == null) {
            return diagnosis;
        }
        
        // Validate and enhance kubectl commands
        List<String> enhancedCommands = enhanceKubectlCommands(diagnosis.getKubectlCommands());
        diagnosis.setKubectlCommands(enhancedCommands);
        
        // Add safety warnings if needed
        addSafetyWarnings(diagnosis);
        
        log.debug("Enhanced recommendations: {} kubectl commands", enhancedCommands.size());
        
        return diagnosis;
    }
    
    /**
     * Enhances kubectl commands with proper formatting and safety checks.
     * 
     * @param commands Original kubectl commands
     * @return Enhanced and validated commands
     */
    private List<String> enhanceKubectlCommands(List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> enhanced = new ArrayList<>();
        
        for (String command : commands) {
            if (command == null || command.isBlank()) {
                continue;
            }
            
            String trimmed = command.trim();
            
            // Skip dangerous commands
            if (isDangerousCommand(trimmed)) {
                log.warn("Skipping potentially dangerous command: {}", trimmed);
                continue;
            }
            
            // Ensure command starts with kubectl
            if (!trimmed.startsWith("kubectl")) {
                trimmed = "kubectl " + trimmed;
            }
            
            // Add dry-run flag for destructive operations if not present
            if (isDestructiveOperation(trimmed) && !trimmed.contains("--dry-run")) {
                trimmed = trimmed + " --dry-run=client";
                log.info("Added --dry-run flag to destructive command");
            }
            
            enhanced.add(trimmed);
        }
        
        return enhanced;
    }
    
    /**
     * Checks if a command is potentially dangerous.
     * 
     * @param command Command to check
     * @return true if dangerous, false otherwise
     */
    private boolean isDangerousCommand(String command) {
        String lower = command.toLowerCase();
        
        // Block commands that could cause cluster-wide damage
        return lower.contains("delete namespace kube-system") ||
               lower.contains("delete all --all-namespaces") ||
               lower.contains("delete node") ||
               lower.contains("rm -rf") ||
               lower.contains("format") ||
               lower.matches(".*delete.*--all.*--all-namespaces.*");
    }
    
    /**
     * Checks if a command is a destructive operation.
     * 
     * @param command Command to check
     * @return true if destructive, false otherwise
     */
    private boolean isDestructiveOperation(String command) {
        String lower = command.toLowerCase();
        
        return lower.contains("delete") ||
               lower.contains("scale") ||
               lower.contains("drain") ||
               lower.contains("cordon") ||
               lower.contains("taint");
    }
    
    /**
     * Adds safety warnings to the diagnosis if needed.
     * 
     * @param diagnosis Diagnosis to enhance
     */
    private void addSafetyWarnings(DiagnosisResult diagnosis) {
        if (diagnosis.getKubectlCommands() == null || diagnosis.getKubectlCommands().isEmpty()) {
            return;
        }
        
        boolean hasDestructiveOps = diagnosis.getKubectlCommands().stream()
                .anyMatch(this::isDestructiveOperation);
        
        if (hasDestructiveOps) {
            String currentFix = diagnosis.getFix();
            String warning = "\n\n⚠️ WARNING: Some commands are destructive operations. " +
                           "Review carefully and test in a non-production environment first. " +
                           "Commands with --dry-run flag should be executed without the flag after verification.";
            
            diagnosis.setFix(currentFix + warning);
            log.info("Added safety warning for destructive operations");
        }
    }
    
    /**
     * Validates that recommendations are actionable and safe.
     * 
     * @param diagnosis Diagnosis to validate
     * @return true if valid, false otherwise
     */
    public boolean validateRecommendations(DiagnosisResult diagnosis) {
        if (diagnosis == null) {
            return false;
        }
        
        // Check that essential fields are present
        if (diagnosis.getRootCause() == null || diagnosis.getRootCause().isBlank()) {
            log.warn("Diagnosis missing root cause");
            return false;
        }
        
        if (diagnosis.getFix() == null || diagnosis.getFix().isBlank()) {
            log.warn("Diagnosis missing fix recommendation");
            return false;
        }
        
        // Validate kubectl commands if present
        if (diagnosis.getKubectlCommands() != null) {
            for (String command : diagnosis.getKubectlCommands()) {
                if (isDangerousCommand(command)) {
                    log.error("Diagnosis contains dangerous command: {}", command);
                    return false;
                }
            }
        }
        
        return true;
    }
}

// Made with Bob
