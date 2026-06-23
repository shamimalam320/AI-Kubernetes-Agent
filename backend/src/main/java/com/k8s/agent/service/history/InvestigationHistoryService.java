package com.k8s.agent.service.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.agent.dto.ai.InvestigationResponse;
import com.k8s.agent.entity.Investigation;
import com.k8s.agent.repository.InvestigationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing investigation history.
 * Stores and retrieves investigation results from database.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvestigationHistoryService {
    
    private final InvestigationRepository repository;
    private final ObjectMapper objectMapper;
    
    /**
     * Save investigation result to database.
     *
     * @param userId User ID
     * @param response Investigation response with diagnosis
     * @return Saved investigation entity
     */
    public Investigation saveInvestigation(String userId, InvestigationResponse response) {
        try {
            String investigationJson = objectMapper.writeValueAsString(response);
            
            Investigation investigation = Investigation.builder()
                    .userId(userId)
                    .rootCause(response.getDiagnosis().getRootCause())
                    .explanation(response.getDiagnosis().getExplanation())
                    .suggestedFix(response.getDiagnosis().getFix())
                    .confidence(response.getDiagnosis().getConfidence())
                    .status("SUCCESS")
                    .investigationData(investigationJson)
                    .build();
            
            Investigation saved = repository.save(investigation);
            log.info("Investigation saved successfully: {} for user: {}", saved.getId(), userId);
            
            return saved;
            
        } catch (Exception e) {
            log.error("Failed to save investigation for user: {}", userId, e);
            throw new RuntimeException("Failed to save investigation", e);
        }
    }
    
    /**
     * Get recent investigations for a user (max 10).
     *
     * @param userId User ID
     * @return List of recent investigations
     */
    public List<Investigation> getUserInvestigations(String userId) {
        log.debug("Fetching investigations for user: {}", userId);
        return repository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get specific investigation by ID for a user.
     *
     * @param id Investigation ID
     * @param userId User ID
     * @return Optional investigation
     */
    public Optional<Investigation> getInvestigation(UUID id, String userId) {
        log.debug("Fetching investigation {} for user: {}", id, userId);
        return repository.findByIdAndUserId(id, userId);
    }
    
    /**
     * Get all investigations for a user.
     *
     * @param userId User ID
     * @return List of all investigations
     */
    public List<Investigation> getAllUserInvestigations(String userId) {
        log.debug("Fetching all investigations for user: {}", userId);
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

// Made with Bob
