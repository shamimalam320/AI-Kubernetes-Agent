package com.k8s.agent.service.ai;

import com.k8s.agent.dto.ai.DiagnosisResult;
import com.k8s.agent.dto.investigation.InvestigationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for calculating and adjusting confidence scores.
 * Validates AI confidence based on evidence quality and investigation completeness.
 */
@Slf4j
@Service
public class ConfidenceEngineService {
    
    /**
     * Adjusts the confidence score based on investigation evidence quality.
     * 
     * @param diagnosis Original diagnosis with AI confidence
     * @param investigation Investigation data used for diagnosis
     * @return Adjusted diagnosis with validated confidence
     */
    public DiagnosisResult adjustConfidence(DiagnosisResult diagnosis, InvestigationResult investigation) {
        if (diagnosis == null || investigation == null) {
            return diagnosis;
        }
        
        int originalConfidence = diagnosis.getConfidence() != null ? diagnosis.getConfidence() : 50;
        int adjustedConfidence = originalConfidence;
        
        // Calculate evidence quality score
        int evidenceScore = calculateEvidenceScore(investigation);
        
        // Adjust confidence based on evidence
        adjustedConfidence = adjustConfidenceBasedOnEvidence(adjustedConfidence, evidenceScore);
        
        // Apply confidence penalties for specific conditions
        adjustedConfidence = applyConfidencePenalties(adjustedConfidence, investigation);
        
        // Ensure confidence is within valid range
        adjustedConfidence = Math.max(0, Math.min(100, adjustedConfidence));
        
        if (adjustedConfidence != originalConfidence) {
            log.info("Adjusted confidence from {} to {} based on evidence quality", 
                    originalConfidence, adjustedConfidence);
        }
        
        diagnosis.setConfidence(adjustedConfidence);
        return diagnosis;
    }
    
    /**
     * Calculates evidence quality score based on investigation completeness.
     * 
     * @param investigation Investigation result
     * @return Evidence score (0-100)
     */
    private int calculateEvidenceScore(InvestigationResult investigation) {
        int score = 0;
        int maxScore = 100;
        
        // Pod inspection evidence (30 points)
        if (investigation.getPodInspection() != null) {
            score += 15;
            if (investigation.getPodInspection().getProblematicPodsCount() > 0) {
                score += 15; // Strong evidence if problematic pods found
            }
        }
        
        // Logs collection evidence (25 points)
        if (investigation.getLogsCollection() != null) {
            score += 10;
            if (investigation.getLogsCollection().getTotalPodsChecked() > 0) {
                score += 15; // Strong evidence if logs collected
            }
        }
        
        // Events analysis evidence (25 points)
        if (investigation.getEventsAnalysis() != null) {
            score += 10;
            if (investigation.getEventsAnalysis().getCriticalEventsCount() > 0) {
                score += 15; // Strong evidence if critical events found
            }
        }
        
        // Deployment inspection evidence (10 points)
        if (investigation.getDeploymentInspection() != null) {
            score += 5;
            if (investigation.getDeploymentInspection().getProblematicDeploymentsCount() > 0) {
                score += 5;
            }
        }
        
        // Network inspection evidence (10 points)
        if (investigation.getNetworkInspection() != null) {
            score += 5;
            if (investigation.getNetworkInspection().getServicesWithIssues() > 0) {
                score += 5;
            }
        }
        
        return Math.min(score, maxScore);
    }
    
    /**
     * Adjusts confidence based on evidence quality.
     * 
     * @param currentConfidence Current confidence score
     * @param evidenceScore Evidence quality score
     * @return Adjusted confidence
     */
    private int adjustConfidenceBasedOnEvidence(int currentConfidence, int evidenceScore) {
        // If evidence is weak (< 40), reduce confidence
        if (evidenceScore < 40) {
            int penalty = (40 - evidenceScore) / 2;
            return currentConfidence - penalty;
        }
        
        // If evidence is strong (> 70), slightly boost confidence
        if (evidenceScore > 70) {
            int boost = (evidenceScore - 70) / 4;
            return currentConfidence + boost;
        }
        
        // Moderate evidence, no adjustment
        return currentConfidence;
    }
    
    /**
     * Applies confidence penalties for specific conditions.
     * 
     * @param currentConfidence Current confidence score
     * @param investigation Investigation result
     * @return Adjusted confidence with penalties
     */
    private int applyConfidencePenalties(int currentConfidence, InvestigationResult investigation) {
        int adjusted = currentConfidence;
        
        // Penalty if cluster appears healthy but diagnosis suggests issues
        if (investigation.isClusterHealthy()) {
            adjusted -= 10;
            log.debug("Applied penalty: cluster appears healthy");
        }
        
        // Penalty if no problematic pods found
        if (investigation.getPodInspection() != null && 
            investigation.getPodInspection().getProblematicPodsCount() == 0) {
            adjusted -= 15;
            log.debug("Applied penalty: no problematic pods found");
        }
        
        // Penalty if no critical events
        if (investigation.getEventsAnalysis() != null && 
            investigation.getEventsAnalysis().getCriticalEventsCount() == 0) {
            adjusted -= 10;
            log.debug("Applied penalty: no critical events found");
        }
        
        // Penalty if no logs collected
        if (investigation.getLogsCollection() != null &&
            investigation.getLogsCollection().getTotalPodsChecked() == 0) {
            adjusted -= 10;
            log.debug("Applied penalty: no logs collected");
        }
        
        return adjusted;
    }
    
    /**
     * Determines if the confidence level is acceptable for the diagnosis.
     * 
     * @param confidence Confidence score
     * @return true if acceptable, false otherwise
     */
    public boolean isConfidenceAcceptable(int confidence) {
        return confidence >= 30; // Minimum threshold for actionable diagnosis
    }
    
    /**
     * Gets a human-readable confidence level description.
     * 
     * @param confidence Confidence score
     * @return Confidence level description
     */
    public String getConfidenceLevel(int confidence) {
        if (confidence >= 80) {
            return "Very High";
        } else if (confidence >= 60) {
            return "High";
        } else if (confidence >= 40) {
            return "Moderate";
        } else if (confidence >= 20) {
            return "Low";
        } else {
            return "Very Low";
        }
    }
}

// Made with Bob
