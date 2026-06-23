package com.k8s.agent.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a message in the OpenRouter API format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenRouterMessage {
    
    /**
     * Role of the message sender: "system", "user", or "assistant"
     */
    private String role;
    
    /**
     * Content of the message
     */
    private String content;
}

// Made with Bob
