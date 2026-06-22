# 03-prompt-ai-reasoning-engine.md

## Context

The Kubernetes Investigation Layer is complete.

We can now collect Kubernetes troubleshooting evidence.

Example:

```text
Pods
Logs
Events
Deployments
Networking
```

Architecture:

```text
React Frontend
    ↓
Spring Boot REST Controller
    ↓
Kubernetes Investigation Layer (Spring Services)
    ↓
AI Kubernetes Agent (Spring Service)
        ↓
LLM Reasoning
(OpenRouter via InsForge Key)
        ↓
Root Cause Analysis
        ↓
Suggested Fix
```

Goal:

We now want to make the system intelligent.

The AI agent should behave like a **Senior Kubernetes SRE**.

It should:

1. Understand Kubernetes failures
2. Correlate logs + events + deployment state
3. Find root cause
4. Suggest fixes
5. Generate confidence score

Important:

Use **OpenRouter API Key provided via InsForge**.

Do not hardcode secrets.

---

## Goal

Build the **AI Kubernetes Agent** using Spring Boot services.

Implement:

```text
PromptBuilderService
OpenRouterClientService
RootCauseAnalyzerService
FixRecommendationService
ConfidenceEngineService
AIService (Orchestrator)
```

The AI layer should consume the investigation payload generated in Prompt 02.

---

## Requirements

### 1. Prompt Builder Service

Create a structured Kubernetes troubleshooting prompt.

The system prompt should make the LLM behave like:

```text
Senior Kubernetes SRE
```

The prompt must include:

```text
Pod Status
Logs
Events
Deployment Health
Networking Findings
```

The AI must return:

```text
1. Root Cause
2. Explanation
3. Suggested Fix
4. kubectl Commands
5. Prevention Recommendation
6. Confidence Score
```

```java
@Service
@Slf4j
public class PromptBuilderService {
    
    public String buildSystemPrompt() {
        return """
            You are a Senior Kubernetes SRE with 10+ years of experience.
            
            Your task is to analyze Kubernetes cluster issues and provide:
            1. Root cause identification
            2. Clear explanation
            3. Actionable fix recommendations
            4. kubectl commands to resolve the issue
            5. Prevention strategies
            6. Confidence score (0-100%)
            
            Be specific, practical, and beginner-friendly.
            Focus on the most likely root cause based on evidence.
            """;
    }
    
    public String buildUserPrompt(InvestigationResult investigation) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Kubernetes Cluster Investigation Results:\n\n");
        
        // Add pod information
        if (investigation.getPods() != null) {
            prompt.append("=== POD STATUS ===\n");
            prompt.append(formatPodInfo(investigation.getPods()));
            prompt.append("\n");
        }
        
        // Add logs
        if (investigation.getLogs() != null) {
            prompt.append("=== LOGS ===\n");
            prompt.append(formatLogs(investigation.getLogs()));
            prompt.append("\n");
        }
        
        // Add events
        if (investigation.getEvents() != null) {
            prompt.append("=== EVENTS ===\n");
            prompt.append(formatEvents(investigation.getEvents()));
            prompt.append("\n");
        }
        
        // Add deployments
        if (investigation.getDeployments() != null) {
            prompt.append("=== DEPLOYMENTS ===\n");
            prompt.append(formatDeployments(investigation.getDeployments()));
            prompt.append("\n");
        }
        
        // Add network
        if (investigation.getNetwork() != null) {
            prompt.append("=== NETWORK ===\n");
            prompt.append(formatNetwork(investigation.getNetwork()));
            prompt.append("\n");
        }
        
        prompt.append("\nProvide a structured diagnosis in JSON format:\n");
        prompt.append("""
            {
              "rootCause": "Brief root cause",
              "explanation": "Detailed explanation",
              "suggestedFix": "Step-by-step fix",
              "kubectlCommands": ["command1", "command2"],
              "prevention": "How to prevent this",
              "confidence": 85
            }
            """);
        
        return prompt.toString();
    }
    
    private String formatPodInfo(PodInspectionResult pods) {
        // Format pod information
    }
    
    // Other formatting methods...
}
```

Prompt should be structured and deterministic.

Avoid vague answers.

---

### 2. OpenRouter Client Service

Use OpenRouter API for LLM access.

Authentication:

```text
OpenRouter API Key from InsForge
```

Configuration (application.yml):

```yaml
openrouter:
  api-key: ${OPENROUTER_API_KEY}
  model: ${OPENROUTER_MODEL:anthropic/claude-3-sonnet}
  api-url: https://openrouter.ai/api/v1/chat/completions
  timeout: 60000
  max-retries: 3
```

Implementation:

```java
@Service
@Slf4j
public class OpenRouterClientService {
    
    @Value("${openrouter.api-key}")
    private String apiKey;
    
    @Value("${openrouter.model}")
    private String model;
    
    @Value("${openrouter.api-url}")
    private String apiUrl;
    
    private final RestTemplate restTemplate;
    
    public OpenRouterClientService(RestTemplateBuilder builder) {
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(60))
            .build();
    }
    
    public String chat(String systemPrompt, String userPrompt) {
        log.info("Sending request to OpenRouter");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "https://ai-k8s-agent.app");
        headers.set("X-Title", "AI Kubernetes Agent");
        
        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ),
            "temperature", 0.3,
            "max_tokens", 2000
        );
        
        HttpEntity<Map<String, Object>> request = 
            new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<OpenRouterResponse> response = 
                restTemplate.postForEntity(
                    apiUrl, 
                    request, 
                    OpenRouterResponse.class
                );
            
            if (response.getStatusCode().is2xxSuccessful() 
                && response.getBody() != null) {
                return response.getBody()
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
            }
            
            throw new RuntimeException("Invalid response from OpenRouter");
            
        } catch (Exception e) {
            log.error("OpenRouter API call failed", e);
            throw new RuntimeException("AI service unavailable", e);
        }
    }
}
```

DTOs:

```java
@Data
public class OpenRouterResponse {
    private List<Choice> choices;
    
    @Data
    public static class Choice {
        private Message message;
    }
    
    @Data
    public static class Message {
        private String content;
    }
}
```

Requirements:

- Use RestTemplate or WebClient
- Add timeout handling
- Handle API failures gracefully
- Add retry logic (optional)
- Log errors cleanly

Do not expose secrets.

Keep implementation simple and explainable.

---

### 3. Root Cause Analyzer Service

Parse and structure the AI response.

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class RootCauseAnalyzerService {
    
    private final ObjectMapper objectMapper;
    
    public DiagnosisResult analyzeDiagnosis(String aiResponse) {
        log.info("Parsing AI diagnosis");
        
        try {
            // Extract JSON from response
            String jsonResponse = extractJson(aiResponse);
            
            // Parse to DTO
            DiagnosisResult diagnosis = objectMapper.readValue(
                jsonResponse, 
                DiagnosisResult.class
            );
            
            // Validate
            validateDiagnosis(diagnosis);
            
            return diagnosis;
            
        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
            throw new RuntimeException("Invalid AI response format", e);
        }
    }
    
    private String extractJson(String response) {
        // Extract JSON from markdown code blocks if present
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            return response.substring(start, end).trim();
        }
        return response;
    }
    
    private void validateDiagnosis(DiagnosisResult diagnosis) {
        if (diagnosis.getRootCause() == null 
            || diagnosis.getRootCause().isBlank()) {
            throw new IllegalArgumentException("Root cause is required");
        }
        
        if (diagnosis.getConfidence() < 0 
            || diagnosis.getConfidence() > 100) {
            throw new IllegalArgumentException("Invalid confidence score");
        }
    }
}
```

DTO:

```java
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DiagnosisResult {
    private String rootCause;
    private String explanation;
    private String suggestedFix;
    private List<String> kubectlCommands;
    private String prevention;
    private Integer confidence;
    private LocalDateTime timestamp;
}
```

The system should correlate evidence instead of blindly summarizing logs.

---

### 4. Fix Recommendation Service

Enhance and validate fix recommendations.

```java
@Service
@Slf4j
public class FixRecommendationService {
    
    public List<String> enhanceKubectlCommands(
        List<String> commands, 
        InvestigationResult investigation
    ) {
        // Add namespace context
        // Validate command syntax
        // Add safety warnings
        return commands;
    }
    
    public String formatFixInstructions(DiagnosisResult diagnosis) {
        StringBuilder instructions = new StringBuilder();
        
        instructions.append("### Root Cause\n");
        instructions.append(diagnosis.getRootCause()).append("\n\n");
        
        instructions.append("### Explanation\n");
        instructions.append(diagnosis.getExplanation()).append("\n\n");
        
        instructions.append("### Suggested Fix\n");
        instructions.append(diagnosis.getSuggestedFix()).append("\n\n");
        
        if (diagnosis.getKubectlCommands() != null 
            && !diagnosis.getKubectlCommands().isEmpty()) {
            instructions.append("### kubectl Commands\n");
            instructions.append("```bash\n");
            diagnosis.getKubectlCommands()
                .forEach(cmd -> instructions.append(cmd).append("\n"));
            instructions.append("```\n\n");
        }
        
        instructions.append("### Prevention\n");
        instructions.append(diagnosis.getPrevention()).append("\n");
        
        return instructions.toString();
    }
}
```

Recommendations must be:

- Practical
- Beginner friendly
- Kubernetes-specific

Avoid generic advice.

---

### 5. Confidence Engine Service

Validate and adjust confidence scores.

```java
@Service
@Slf4j
public class ConfidenceEngineService {
    
    public Integer calculateConfidence(
        DiagnosisResult diagnosis,
        InvestigationResult investigation
    ) {
        int baseConfidence = diagnosis.getConfidence();
        
        // Adjust based on evidence quality
        if (hasStrongEvidence(investigation)) {
            baseConfidence = Math.min(100, baseConfidence + 10);
        }
        
        if (hasConflictingEvidence(investigation)) {
            baseConfidence = Math.max(0, baseConfidence - 20);
        }
        
        return baseConfidence;
    }
    
    private boolean hasStrongEvidence(InvestigationResult investigation) {
        // Check if we have clear error messages in logs
        // Check if events clearly indicate the issue
        return false;
    }
    
    private boolean hasConflictingEvidence(InvestigationResult investigation) {
        // Check for contradictory signals
        return false;
    }
}
```

---

### 6. AI Service (Orchestrator)

Main service that orchestrates the AI reasoning flow.

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class AIService {
    
    private final PromptBuilderService promptBuilderService;
    private final OpenRouterClientService openRouterClient;
    private final RootCauseAnalyzerService rootCauseAnalyzer;
    private final FixRecommendationService fixRecommendationService;
    private final ConfidenceEngineService confidenceEngine;
    
    public DiagnosisResult diagnose(InvestigationResult investigation) {
        log.info("Starting AI diagnosis");
        
        try {
            // 1. Build prompts
            String systemPrompt = promptBuilderService.buildSystemPrompt();
            String userPrompt = promptBuilderService.buildUserPrompt(investigation);
            
            // 2. Call LLM
            String aiResponse = openRouterClient.chat(systemPrompt, userPrompt);
            log.debug("AI Response: {}", aiResponse);
            
            // 3. Parse response
            DiagnosisResult diagnosis = rootCauseAnalyzer.analyzeDiagnosis(aiResponse);
            
            // 4. Enhance recommendations
            List<String> enhancedCommands = fixRecommendationService
                .enhanceKubectlCommands(
                    diagnosis.getKubectlCommands(), 
                    investigation
                );
            diagnosis.setKubectlCommands(enhancedCommands);
            
            // 5. Adjust confidence
            Integer adjustedConfidence = confidenceEngine.calculateConfidence(
                diagnosis, 
                investigation
            );
            diagnosis.setConfidence(adjustedConfidence);
            
            // 6. Set timestamp
            diagnosis.setTimestamp(LocalDateTime.now());
            
            log.info("Diagnosis completed with {}% confidence", 
                diagnosis.getConfidence());
            
            return diagnosis;
            
        } catch (Exception e) {
            log.error("AI diagnosis failed", e);
            throw new RuntimeException("Failed to generate diagnosis", e);
        }
    }
}
```

---

## Spring Boot REST API Integration

Update InvestigationController:

```java
@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class InvestigationController {
    
    private final InvestigationService investigationService;
    private final AIService aiService;
    
    @PostMapping("/investigate")
    public ResponseEntity<ApiResponse<InvestigationResponse>> investigate() {
        log.info("Received investigation request");
        
        try {
            // 1. Collect Kubernetes evidence
            InvestigationResult investigation = 
                investigationService.investigate();
            
            // 2. AI diagnosis
            DiagnosisResult diagnosis = aiService.diagnose(investigation);
            
            // 3. Build response
            InvestigationResponse response = InvestigationResponse.builder()
                .investigation(investigation)
                .diagnosis(diagnosis)
                .build();
            
            return ResponseEntity.ok(
                ApiResponse.<InvestigationResponse>builder()
                    .status("success")
                    .data(response)
                    .message("Investigation completed successfully")
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Investigation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<InvestigationResponse>builder()
                    .status("error")
                    .message("Investigation failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
                );
        }
    }
}
```

Response DTO:

```java
@Data
@Builder
public class InvestigationResponse {
    private InvestigationResult investigation;
    private DiagnosisResult diagnosis;
}
```

Example API response:

```json
{
  "status": "success",
  "data": {
    "investigation": {
      "pods": { ... },
      "logs": { ... },
      "events": { ... },
      "deployments": { ... },
      "network": { ... }
    },
    "diagnosis": {
      "root_cause": "DATABASE_URL environment variable missing",
      "explanation": "The payment-service pod is in CrashLoopBackOff state because the application cannot start without the DATABASE_URL environment variable.",
      "suggested_fix": "Add the DATABASE_URL environment variable to the deployment configuration",
      "kubectl_commands": [
        "kubectl edit deployment payment-service -n default",
        "# Add under spec.template.spec.containers[0].env:",
        "# - name: DATABASE_URL",
        "#   value: postgresql://..."
      ],
      "prevention": "Use ConfigMaps or Secrets for environment variables and validate required env vars at startup",
      "confidence": 92,
      "timestamp": "2024-01-01T00:00:00"
    }
  },
  "message": "Investigation completed successfully",
  "timestamp": "2024-01-01T00:00:00"
}
```

---

## Maven Dependencies

Add to pom.xml:

```xml
<dependencies>
    <!-- Existing dependencies -->
    
    <!-- Jackson for JSON processing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>
</dependencies>
```

---

## Constraints

DO NOT implement:

- Authentication
- Investigation history
- Realtime updates
- Frontend changes
- Deployment

Only build the AI reasoning layer.

Keep implementation enterprise-grade.

Do not overengineer.

DO NOT BREAK EXISTING CODE.

Only extend existing functionality.

---

## Expected Result

When I call:

```http
POST http://localhost:8080/api/v1/investigate
```

The system should:

```text
Investigate Kubernetes
        ↓
Collect Evidence
        ↓
Send to AI Agent
        ↓
LLM Reasoning (OpenRouter)
        ↓
Parse Diagnosis
        ↓
Enhance Recommendations
        ↓
Return Complete Diagnosis
```

The backend should now behave like:

> A Senior Kubernetes SRE helping troubleshoot incidents.

---

## Best Practices

### Java/Spring Boot
- Use `@Service` for business logic
- Use `@RequiredArgsConstructor` for dependency injection
- Use `@Slf4j` for logging
- Handle exceptions gracefully
- Validate all inputs
- Use DTOs for API responses

### AI Integration
- Keep prompts structured and clear
- Parse AI responses defensively
- Validate confidence scores
- Log all AI interactions
- Handle API failures gracefully
- Set reasonable timeouts

### Security
- Never log API keys
- Use environment variables for secrets
- Validate all AI responses
- Sanitize user inputs
- Add rate limiting (future)

### Performance
- Set appropriate timeouts
- Consider caching for similar issues
- Limit prompt size
- Use async processing for large clusters (future)
