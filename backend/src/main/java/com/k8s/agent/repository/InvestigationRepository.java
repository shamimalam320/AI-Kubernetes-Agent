package com.k8s.agent.repository;

import com.k8s.agent.entity.Investigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Investigation entity.
 * Provides database operations for investigation history.
 */
@Repository
public interface InvestigationRepository extends JpaRepository<Investigation, UUID> {
    
    /**
     * Find all investigations for a user, ordered by creation date descending.
     *
     * @param userId User ID
     * @return List of investigations
     */
    List<Investigation> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find top 10 investigations for a user, ordered by creation date descending.
     *
     * @param userId User ID
     * @return List of investigations (max 10)
     */
    List<Investigation> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find investigation by ID and user ID.
     *
     * @param id Investigation ID
     * @param userId User ID
     * @return Optional investigation
     */
    Optional<Investigation> findByIdAndUserId(UUID id, String userId);
}

// Made with Bob
