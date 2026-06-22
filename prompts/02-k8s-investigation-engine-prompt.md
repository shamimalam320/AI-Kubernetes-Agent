# 02-prompt-kubernetes-investigation-engine.md

## Context

Project setup is already complete.

We now want to build the **Kubernetes Investigation Layer**.

Architecture:

```text
React Frontend
    ↓
Spring Boot REST Controller
    ↓
Kubernetes Investigation Layer (Spring Services)
        ├── Check Pods
        ├── Read Logs
        ├── Analyze Events
        ├── Inspect Deployments
        └── Check Networking
    ↓
AI Kubernetes Agent
```

Goal:

This layer should behave like a **junior DevOps engineer collecting evidence** before AI reasoning starts.

Important:

We are still **NOT implementing AI reasoning**.

We are only collecting Kubernetes troubleshooting data.

Use **Kubernetes Java Client** (official Kubernetes client for Java).

---

## Goal

Build the Kubernetes Investigation Layer using Spring Boot services.

Implement:

```text
PodInspectorService
LogsCollectorService
EventsAnalyzerService
DeploymentInspectorService
NetworkInspectorService
KubernetesClientConfig
InvestigationService (Orchestrator)
```

The Spring Boot REST controller should orchestrate these components.

---

## Requirements

### 1. Kubernetes Client Configuration

Create a Spring configuration for Kubernetes Java Client.

Requirements:

- Load kubeconfig from file or in-cluster config
- Create ApiClient bean
- Configure CoreV1Api, AppsV1Api, EventsV1Api beans
- Handle connection failures gracefully
- Add logging

Example Maven dependency:

```xml
<dependency>
    <groupId>io.kubernetes</groupId>
    <artifactId>client-java</artifactId>
    <version>19.0.0</version>
</dependency>
```

Configuration class:

```java
@Configuration
@Slf4j
public class KubernetesClientConfig {
    
    @Value("${kubernetes.config-path}")
    private String kubeconfigPath;
    
    @Bean
    public ApiClient apiClient() throws IOException {
        // Load from kubeconfig or in-cluster
    }
    
    @Bean
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }
    
    @Bean
    public AppsV1Api appsV1Api(ApiClient apiClient) {
        return new AppsV1Api(apiClient);
    }
}
```

Keep implementation clean and enterprise-grade.

---

### 2. Pod Inspector Service

Responsibilities:

- Get pod status across all namespaces
- Detect unhealthy pods

Detect:

```text
CrashLoopBackOff
ImagePullBackOff
Pending
Error
OOMKilled
ContainerCreating (stuck)
```

Return structured DTO.

Example:

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class PodInspectorService {
    
    private final CoreV1Api coreV1Api;
    
    public PodInspectionResult inspectPods() {
        // Get all pods
        // Check status
        // Identify problematic pods
        // Return structured result
    }
}
```

DTO Example:

```java
@Data
@Builder
public class PodInspectionResult {
    private boolean healthy;
    private List<ProblematicPod> problematicPods;
    private int totalPods;
    private int healthyPods;
}

@Data
@Builder
public class ProblematicPod {
    private String name;
    private String namespace;
    private String status;
    private String reason;
    private int restartCount;
}
```

---

### 3. Logs Collector Service

Responsibilities:

- Fetch logs for failed pods
- Capture relevant failures
- Limit log size

Focus on:

```text
Exceptions
Connection failures
Missing env vars
Image failures
Startup errors
```

Keep logs concise (last 100 lines or errors only).

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class LogsCollectorService {
    
    private final CoreV1Api coreV1Api;
    
    public LogsCollectionResult collectLogs(List<ProblematicPod> pods) {
        // For each problematic pod
        // Fetch logs (last 100 lines)
        // Extract error patterns
        // Return structured result
    }
}
```

---

### 4. Events Analyzer Service

Responsibilities:

Read Kubernetes events and detect issues.

Detect:

```text
FailedScheduling
BackOff
FailedMount
FailedPull
ErrImagePull
Unhealthy
```

Return summarized findings.

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class EventsAnalyzerService {
    
    private final CoreV1Api coreV1Api;
    
    public EventsAnalysisResult analyzeEvents() {
        // Get recent events (last 1 hour)
        // Filter warning/error events
        // Categorize by type
        // Return structured result
    }
}
```

---

### 5. Deployment Inspector Service

Responsibilities:

Inspect deployments for issues.

Check:

```text
Available replicas
Unavailable replicas
Rollout failures
Deployment conditions
```

Detect unhealthy deployments.

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class DeploymentInspectorService {
    
    private final AppsV1Api appsV1Api;
    
    public DeploymentInspectionResult inspectDeployments() {
        // Get all deployments
        // Check replica status
        // Check conditions
        // Identify unhealthy deployments
        // Return structured result
    }
}
```

---

### 6. Network Inspector Service

Responsibilities:

Inspect services and networking.

Check:

```text
Service existence
Selector mismatch
Missing endpoints
Service type issues
```

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class NetworkInspectorService {
    
    private final CoreV1Api coreV1Api;
    
    public NetworkInspectionResult inspectNetwork() {
        // Get all services
        // Check endpoints
        // Validate selectors
        // Return structured result
    }
}
```

---

### 7. Investigation Service (Orchestrator)

Create a service that orchestrates everything.

Flow:

```text
Check Pods
    ↓
Collect Logs (for problematic pods)
    ↓
Analyze Events
    ↓
Inspect Deployments
    ↓
Check Networking
```

Return a single structured investigation payload.

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class InvestigationService {
    
    private final PodInspectorService podInspectorService;
    private final LogsCollectorService logsCollectorService;
    private final EventsAnalyzerService eventsAnalyzerService;
    private final DeploymentInspectorService deploymentInspectorService;
    private final NetworkInspectorService networkInspectorService;
    
    public InvestigationResult investigate() {
        log.info("Starting Kubernetes cluster investigation");
        
        // 1. Inspect pods
        PodInspectionResult pods = podInspectorService.inspectPods();
        
        // 2. Collect logs for problematic pods
        LogsCollectionResult logs = logsCollectorService.collectLogs(
            pods.getProblematicPods()
        );
        
        // 3. Analyze events
        EventsAnalysisResult events = eventsAnalyzerService.analyzeEvents();
        
        // 4. Inspect deployments
        DeploymentInspectionResult deployments = 
            deploymentInspectorService.inspectDeployments();
        
        // 5. Inspect network
        NetworkInspectionResult network = 
            networkInspectorService.inspectNetwork();
        
        // Build comprehensive result
        return InvestigationResult.builder()
            .pods(pods)
            .logs(logs)
            .events(events)
            .deployments(deployments)
            .network(network)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

DTO:

```java
@Data
@Builder
public class InvestigationResult {
    private PodInspectionResult pods;
    private LogsCollectionResult logs;
    private EventsAnalysisResult events;
    private DeploymentInspectionResult deployments;
    private NetworkInspectionResult network;
    private LocalDateTime timestamp;
}
```

---

## Spring Boot REST API

Create REST controller:

```java
@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class InvestigationController {
    
    private final InvestigationService investigationService;
    
    @PostMapping("/investigate")
    public ResponseEntity<ApiResponse<InvestigationResult>> investigate() {
        log.info("Received investigation request");
        
        try {
            InvestigationResult result = investigationService.investigate();
            
            return ResponseEntity.ok(
                ApiResponse.<InvestigationResult>builder()
                    .status("success")
                    .data(result)
                    .message("Investigation completed successfully")
                    .build()
            );
        } catch (Exception e) {
            log.error("Investigation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<InvestigationResult>builder()
                    .status("error")
                    .message("Investigation failed: " + e.getMessage())
                    .build()
                );
        }
    }
}
```

Generic API Response:

```java
@Data
@Builder
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
    private LocalDateTime timestamp;
}
```

Example response:

```json
{
  "status": "success",
  "data": {
    "pods": {
      "healthy": false,
      "problematicPods": [
        {
          "name": "payment-service-abc123",
          "namespace": "default",
          "status": "CrashLoopBackOff",
          "reason": "Error",
          "restartCount": 5
        }
      ],
      "totalPods": 10,
      "healthyPods": 9
    },
    "logs": {},
    "events": {},
    "deployments": {},
    "network": {},
    "timestamp": "2024-01-01T00:00:00"
  },
  "message": "Investigation completed successfully",
  "timestamp": "2024-01-01T00:00:00"
}
```

No AI yet.

No root cause analysis yet.

This step is only evidence gathering.

---

## Exception Handling

Add global exception handler:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleKubernetesException(
        ApiException e
    ) {
        log.error("Kubernetes API error", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.<Void>builder()
                .status("error")
                .message("Unable to connect to Kubernetes cluster")
                .build()
            );
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
        Exception e
    ) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.<Void>builder()
                .status("error")
                .message("Internal server error")
                .build()
            );
    }
}
```

---

## Maven Dependencies

Add to pom.xml:

```xml
<dependencies>
    <!-- Kubernetes Java Client -->
    <dependency>
        <groupId>io.kubernetes</groupId>
        <artifactId>client-java</artifactId>
        <version>19.0.0</version>
    </dependency>
    
    <!-- Existing dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

---

## Constraints

DO NOT implement:

- OpenRouter integration
- LLM reasoning
- Root cause analysis
- Fix recommendation
- InsForge integration
- Authentication
- Realtime updates

Use **Kubernetes Java Client** (official library).

Keep code modular and enterprise-grade.

DO NOT BREAK EXISTING CODE.

Only extend the project incrementally.

---

## Expected Result

I should be able to call:

```http
POST http://localhost:8080/api/v1/investigate
```

And receive structured Kubernetes troubleshooting evidence.

The backend should now behave like:

> A junior DevOps engineer collecting debugging evidence.

---

## Best Practices

### Java/Spring Boot
- Use `@Service` for business logic
- Use `@RequiredArgsConstructor` for constructor injection
- Use `@Slf4j` for logging
- Use `@Builder` for DTOs
- Handle exceptions gracefully
- Add meaningful log messages

### Kubernetes Client
- Always handle ApiException
- Use try-catch for API calls
- Set reasonable timeouts
- Filter data to avoid large responses
- Use label selectors when possible

### Performance
- Limit log collection (last 100 lines)
- Filter events (last 1 hour)
- Use pagination for large lists
- Avoid blocking operations
- Consider async processing for large clusters

### Code Quality
- Follow SOLID principles
- Keep methods small and focused
- Use meaningful variable names
- Add JavaDoc for public methods
- Write unit tests
