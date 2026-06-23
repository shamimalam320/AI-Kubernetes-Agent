package com.k8s.agent.service.ai;

import com.k8s.agent.dto.ai.OpenRouterMessage;
import com.k8s.agent.dto.ai.OpenRouterRequest;
import com.k8s.agent.dto.ai.OpenRouterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service responsible for communicating with the OpenRouter API.
 * Handles HTTP requests, authentication, and error handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenRouterClientService {
    
    private final RestTemplate restTemplate;
    
    @Value("${openrouter.api-key}")
    private String apiKey;
    
    @Value("${openrouter.api-url}")
    private String apiUrl;
    
    @Value("${openrouter.model}")
    private String model;
    
    @Value("${openrouter.temperature}")
    private Double temperature;
    
    @Value("${openrouter.max-tokens}")
    private Integer maxTokens;
    
    @Value("${openrouter.enabled}")
    private Boolean enabled;
    
    /**
     * Sends a chat completion request to OpenRouter API.
     * 
     * @param systemPrompt System prompt defining AI behavior
     * @param userPrompt User prompt with investigation data
     * @return OpenRouter API response
     * @throws RuntimeException if API call fails
     */
    public OpenRouterResponse sendChatCompletion(String systemPrompt, String userPrompt) {
        if (!enabled) {
            throw new RuntimeException("OpenRouter integration is disabled");
        }
        
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OpenRouter API key is not configured");
        }
        
        try {
            // Build request
            OpenRouterRequest request = OpenRouterRequest.builder()
                    .model(model)
                    .messages(List.of(
                            OpenRouterMessage.builder()
                                    .role("system")
                                    .content(systemPrompt)
                                    .build(),
                            OpenRouterMessage.builder()
                                    .role("user")
                                    .content(userPrompt)
                                    .build()
                    ))
                    .temperature(temperature)
                    .max_tokens(maxTokens)
                    .stream(false)
                    .build();
            
            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("HTTP-Referer", "https://github.com/ai-k8s-agent");
            headers.set("X-Title", "AI Kubernetes Agent");
            
            // Create HTTP entity
            HttpEntity<OpenRouterRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending request to OpenRouter API: model={}, temperature={}", model, temperature);
            
            // Send request
            ResponseEntity<OpenRouterResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    OpenRouterResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully received response from OpenRouter API");
                return response.getBody();
            } else {
                throw new RuntimeException("OpenRouter API returned unsuccessful status: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            log.error("Client error calling OpenRouter API: status={}, body={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("OpenRouter API client error: " + e.getMessage(), e);
            
        } catch (HttpServerErrorException e) {
            log.error("Server error calling OpenRouter API: status={}, body={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("OpenRouter API server error: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Unexpected error calling OpenRouter API", e);
            throw new RuntimeException("Failed to call OpenRouter API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates that OpenRouter is properly configured.
     * 
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}

// Made with Bob
