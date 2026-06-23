package com.k8s.agent.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a request to the OpenRouter API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenRouterRequest {
    
    /**
     * Model identifier (e.g., "anthropic/claude-3-sonnet")
     */
    private String model;
    
    /**
     * List of messages in the conversation
     */
    private List<OpenRouterMessage> messages;
    
    /**
     * Temperature for response randomness (0.0 - 2.0)
     * Lower values = more focused and deterministic
     */
    private Double temperature;
    
    /**
     * Maximum number of tokens to generate
     */
    @Builder.Default
    private Integer max_tokens = 2000;
    
    /**
     * Whether to stream the response
     */
    @Builder.Default
    private Boolean stream = false;
}

// Made with Bob
