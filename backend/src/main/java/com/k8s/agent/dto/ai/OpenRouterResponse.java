package com.k8s.agent.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a response from the OpenRouter API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenRouterResponse {
    
    /**
     * Unique identifier for the response
     */
    private String id;
    
    /**
     * List of choices (typically contains one choice)
     */
    private List<Choice> choices;
    
    /**
     * Model used for generation
     */
    private String model;
    
    /**
     * Usage statistics
     */
    private Usage usage;
    
    /**
     * Nested class representing a choice in the response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        /**
         * Index of the choice
         */
        private Integer index;
        
        /**
         * Message content
         */
        private OpenRouterMessage message;
        
        /**
         * Finish reason (e.g., "stop", "length")
         */
        private String finish_reason;
    }
    
    /**
     * Nested class representing usage statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * Number of tokens in the prompt
         */
        private Integer prompt_tokens;
        
        /**
         * Number of tokens in the completion
         */
        private Integer completion_tokens;
        
        /**
         * Total tokens used
         */
        private Integer total_tokens;
    }
}

// Made with Bob
