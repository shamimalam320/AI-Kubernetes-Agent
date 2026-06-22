# 05-prompt-end-to-end-integration.md

## Context

The application is now complete.

Users can:

```text
Login
        ↓
Click "Investigate Cluster"
        ↓
Backend investigates Kubernetes
        ↓
AI reasons about failures
        ↓
Root cause generated
        ↓
Suggested fix shown
        ↓
Investigation history saved
```

Architecture:

```text
React Frontend
    ↓
Spring Boot REST API
    ↓
Kubernetes Investigation Layer (Spring Services)
    ↓
AI Kubernetes Agent (Spring Service)
    ↓
LLM Reasoning
(OpenRouter via InsForge Key)
    ↓
Root Cause + Suggested Fix
    ↓
InsForge
(Auth + PostgreSQL + WebSocket)
    ↓
Frontend Diagnosis
```

Goal:

We now want to:

1. Integrate everything end-to-end
2. Test real Kubernetes failures
3. Improve reliability
4. Add cluster selection feature

This step should make the system feel like a **real product**.

---

## Goal

Implement:

```text
End-to-End Integration
Error Handling
Loading States
Cluster Selection
Real Kubernetes Failure Testing
Production Readiness
```

Keep implementation enterprise-grade.

---

## Requirements

### 1. End-to-End Integration

Validate the full flow.

Expected workflow:

```text
User clicks Investigate
        ↓
Frontend sends API request with JWT
        ↓
Spring Boot validates authentication
        ↓
Kubernetes evidence collected (Java Client)
        ↓
AI reasoning triggered (OpenRouter)
        ↓
Root cause generated
        ↓
Suggested fix returned
        ↓
History saved (PostgreSQL via InsForge)
        ↓
WebSocket updates sent
        ↓
User sees diagnosis
```

Ensure all components work together.

Fix integration issues if needed.

---

### 2. Cluster Selection Feature

Allow users to select which Kubernetes cluster to investigate.

Backend Service:

```java
@Service
@Slf4j
public class ClusterService {
    
    @Value("${kubernetes.config-path}")
    private String kubeconfigPath;
    
    public List<ClusterInfo> getAvailableClusters() {
        try {
            // Parse kubeconfig file
            File configFile = new File(kubeconfigPath);
            KubeConfig kubeConfig = KubeConfig.loadKubeConfig(
                new FileReader(configFile)
            );
            
            List<ClusterInfo> clusters = new ArrayList<>();
            
            for (Object contextObj : kubeConfig.getContexts()) {
                Map<String, Object> context = (Map<String, Object>) contextObj;
                String name = (String) context.get("name");
                
                clusters.add(ClusterInfo.builder()
                    .name(name)
                    .isActive(name.equals(kubeConfig.getCurrentContext()))
                    .build()
                );
            }
            
            return clusters;
            
        } catch (Exception e) {
            log.error("Failed to load clusters", e);
            throw new RuntimeException("Failed to load Kubernetes clusters", e);
        }
    }
    
    public void switchCluster(String clusterName) {
        try {
            File configFile = new File(kubeconfigPath);
            KubeConfig kubeConfig = KubeConfig.loadKubeConfig(
                new FileReader(configFile)
            );
            
            kubeConfig.setContext(clusterName);
            
            log.info("Switched to cluster: {}", clusterName);
            
        } catch (Exception e) {
            log.error("Failed to switch cluster", e);
            throw new RuntimeException("Failed to switch cluster", e);
        }
    }
}

@Data
@Builder
class ClusterInfo {
    private String name;
    private boolean isActive;
}
```

REST Controller:

```java
@RestController
@RequestMapping("/api/v1/clusters")
@Slf4j
@RequiredArgsConstructor
public class ClusterController {
    
    private final ClusterService clusterService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClusterInfo>>> getClusters() {
        List<ClusterInfo> clusters = clusterService.getAvailableClusters();
        
        return ResponseEntity.ok(
            ApiResponse.<List<ClusterInfo>>builder()
                .status("success")
                .data(clusters)
                .message("Clusters retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @PostMapping("/switch")
    public ResponseEntity<ApiResponse<Void>> switchCluster(
        @RequestBody SwitchClusterRequest request
    ) {
        clusterService.switchCluster(request.getClusterName());
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .status("success")
                .message("Cluster switched successfully")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}

@Data
class SwitchClusterRequest {
    private String clusterName;
}
```

Frontend Component:

```typescript
// components/ClusterSelector.tsx
import React from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiService } from '../services/api';

interface Cluster {
  name: string;
  isActive: boolean;
}

const ClusterSelector: React.FC = () => {
  const { data: clusters, isLoading } = useQuery({
    queryKey: ['clusters'],
    queryFn: apiService.getClusters,
  });

  const switchCluster = useMutation({
    mutationFn: apiService.switchCluster,
    onSuccess: () => {
      // Refresh clusters list
    },
  });

  if (isLoading) return <div>Loading clusters...</div>;

  return (
    <div className="bg-white rounded-lg shadow p-4 mb-6">
      <h3 className="text-lg font-semibold mb-3">Select Cluster</h3>
      <div className="space-y-2">
        {clusters?.map((cluster: Cluster) => (
          <button
            key={cluster.name}
            onClick={() => switchCluster.mutate(cluster.name)}
            className={`w-full text-left px-4 py-2 rounded-lg transition-colors ${
              cluster.isActive
                ? 'bg-blue-100 text-blue-800 font-semibold'
                : 'bg-gray-50 hover:bg-gray-100'
            }`}
          >
            {cluster.name}
            {cluster.isActive && (
              <span className="ml-2 text-sm">(Active)</span>
            )}
          </button>
        ))}
      </div>
    </div>
  );
};

export default ClusterSelector;
```

---

### 3. Improve Reliability

Add comprehensive error handling.

Global Exception Handler:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleKubernetesException(
        ApiException e
    ) {
        log.error("Kubernetes API error", e);
        
        String message = buildUserFriendlyMessage(e);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.<Void>builder()
                .status("error")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build()
            );
    }
    
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Void>> handleIOException(
        IOException e
    ) {
        log.error("IO error", e);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.<Void>builder()
                .status("error")
                .message("Unable to connect to Kubernetes cluster. " +
                        "Please verify:\n" +
                        "- kubeconfig path is correct\n" +
                        "- cluster is accessible\n" +
                        "- kubectl permissions are valid")
                .timestamp(LocalDateTime.now())
                .build()
            );
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
        RuntimeException e
    ) {
        log.error("Runtime error", e);
        
        if (e.getMessage().contains("OpenRouter")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<Void>builder()
                    .status("error")
                    .message("AI service temporarily unavailable. " +
                            "Please try again in a moment.")
                    .timestamp(LocalDateTime.now())
                    .build()
                );
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.<Void>builder()
                .status("error")
                .message("An unexpected error occurred. " +
                        "Please contact support if the issue persists.")
                .timestamp(LocalDateTime.now())
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
                .timestamp(LocalDateTime.now())
                .build()
            );
    }
    
    private String buildUserFriendlyMessage(ApiException e) {
        if (e.getCode() == 401 || e.getCode() == 403) {
            return "Authentication failed. Please check your kubeconfig credentials.";
        }
        
        if (e.getCode() == 404) {
            return "Kubernetes resource not found. The cluster may be empty.";
        }
        
        if (e.getCode() >= 500) {
            return "Kubernetes API server error. Please check cluster health.";
        }
        
        return "Unable to communicate with Kubernetes cluster.";
    }
}
```

Frontend Error Handling:

```typescript
// components/ErrorDisplay.tsx
import React from 'react';

interface Props {
  error: string;
  onRetry?: () => void;
}

const ErrorDisplay: React.FC<Props> = ({ error, onRetry }) => {
  return (
    <div className="bg-red-50 border border-red-200 rounded-lg p-6">
      <div className="flex items-start">
        <div className="flex-shrink-0">
          <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div className="ml-3 flex-1">
          <h3 className="text-sm font-medium text-red-800">
            Investigation Failed
          </h3>
          <div className="mt-2 text-sm text-red-700 whitespace-pre-line">
            {error}
          </div>
          {onRetry && (
            <div className="mt-4">
              <button
                onClick={onRetry}
                className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
              >
                Retry Investigation
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ErrorDisplay;
```

---

### 4. Loading & Empty States

Improve UX with proper states.

Empty State Component:

```typescript
// components/EmptyState.tsx
import React from 'react';

interface Props {
  title: string;
  message: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

const EmptyState: React.FC<Props> = ({ title, message, action }) => {
  return (
    <div className="bg-white rounded-lg shadow p-12 text-center">
      <svg
        className="mx-auto h-12 w-12 text-gray-400"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <h3 className="mt-4 text-lg font-medium text-gray-900">{title}</h3>
      <p className="mt-2 text-sm text-gray-500">{message}</p>
      {action && (
        <button
          onClick={action.onClick}
          className="mt-6 bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          {action.label}
        </button>
      )}
    </div>
  );
};

export default EmptyState;
```

Handle "No Issues Found":

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class InvestigationService {
    
    public InvestigationResult investigate(String userId) {
        // ... collect evidence
        
        // Check if any issues found
        boolean hasIssues = pods.getProblematicPods().size() > 0 ||
                           events.getWarningEvents().size() > 0 ||
                           deployments.getUnhealthyDeployments().size() > 0;
        
        if (!hasIssues) {
            log.info("No issues found in cluster");
            return InvestigationResult.builder()
                .healthy(true)
                .message("No critical Kubernetes issues detected. " +
                        "Cluster appears healthy.")
                .timestamp(LocalDateTime.now())
                .build();
        }
        
        // Continue with normal investigation
        return result;
    }
}
```

---

### 5. Test Real Kubernetes Failures

Create test scenarios for validation.

Test Deployment YAMLs:

```yaml
# test-scenarios/01-crashloop.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: test-crashloop
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: test-crashloop
  template:
    metadata:
      labels:
        app: test-crashloop
    spec:
      containers:
      - name: app
        image: busybox
        command: ["sh", "-c", "echo Missing DATABASE_URL && exit 1"]
        env:
        - name: REQUIRED_VAR
          value: ""  # Missing value causes crash

---
# test-scenarios/02-imagepull.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: test-imagepull
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: test-imagepull
  template:
    metadata:
      labels:
        app: test-imagepull
    spec:
      containers:
      - name: app
        image: nginx:invalid-tag-12345  # Invalid image tag

---
# test-scenarios/03-oom.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: test-oom
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: test-oom
  template:
    metadata:
      labels:
        app: test-oom
    spec:
      containers:
      - name: app
        image: progrium/stress
        args: ["--vm", "1", "--vm-bytes", "500M"]
        resources:
          limits:
            memory: "128Mi"  # Too low, will cause OOMKilled

---
# test-scenarios/04-selector-mismatch.yaml
apiVersion: v1
kind: Service
metadata:
  name: test-service
  namespace: default
spec:
  selector:
    app: wrong-label  # Mismatch with deployment
  ports:
  - port: 80
    targetPort: 8080
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: test-deployment
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: test-app  # Different from service selector
  template:
    metadata:
      labels:
        app: test-app
    spec:
      containers:
      - name: app
        image: nginx
```

Testing Script:

```bash
#!/bin/bash
# test-scenarios/run-tests.sh

echo "Testing AI Kubernetes Agent"
echo "============================"

# Test 1: CrashLoopBackOff
echo "\n1. Testing CrashLoopBackOff scenario..."
kubectl apply -f 01-crashloop.yaml
sleep 30
echo "Trigger investigation and verify AI detects missing env var"

# Test 2: ImagePullBackOff
echo "\n2. Testing ImagePullBackOff scenario..."
kubectl apply -f 02-imagepull.yaml
sleep 30
echo "Trigger investigation and verify AI detects invalid image"

# Test 3: OOMKilled
echo "\n3. Testing OOMKilled scenario..."
kubectl apply -f 03-oom.yaml
sleep 30
echo "Trigger investigation and verify AI detects memory limit issue"

# Test 4: Service Selector Mismatch
echo "\n4. Testing Service Selector Mismatch..."
kubectl apply -f 04-selector-mismatch.yaml
sleep 30
echo "Trigger investigation and verify AI detects selector mismatch"

# Cleanup
echo "\nCleaning up test resources..."
kubectl delete -f 01-crashloop.yaml
kubectl delete -f 02-imagepull.yaml
kubectl delete -f 03-oom.yaml
kubectl delete -f 04-selector-mismatch.yaml

echo "\nTests complete!"
```

---

### 6. Production Readiness

Add production configurations.

application-prod.yml:

```yaml
server:
  port: 8080
  compression:
    enabled: true

spring:
  application:
    name: ai-kubernetes-agent
  
  datasource:
    url: ${INSFORGE_DATABASE_URL}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: false

logging:
  level:
    root: INFO
    com.k8s.agent: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

kubernetes:
  config-path: ${KUBECONFIG_PATH:/root/.kube/config}

openrouter:
  api-key: ${OPENROUTER_API_KEY}
  model: ${OPENROUTER_MODEL:anthropic/claude-3-sonnet}
  timeout: 60000

insforge:
  api-key: ${INSFORGE_API_KEY}
  api-base-url: ${INSFORGE_API_BASE_URL}
  jwt-secret: ${INSFORGE_JWT_SECRET}
```

Docker Compose for Production:

```yaml
version: '3.8'

services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - INSFORGE_DATABASE_URL=${INSFORGE_DATABASE_URL}
      - INSFORGE_API_KEY=${INSFORGE_API_KEY}
      - INSFORGE_API_BASE_URL=${INSFORGE_API_BASE_URL}
      - INSFORGE_JWT_SECRET=${INSFORGE_JWT_SECRET}
      - OPENROUTER_API_KEY=${OPENROUTER_API_KEY}
      - KUBECONFIG_PATH=/root/.kube/config
    volumes:
      - ~/.kube/config:/root/.kube/config:ro
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "3000:80"
    depends_on:
      - backend
    environment:
      - VITE_API_BASE_URL=http://localhost:8080
    restart: unless-stopped
```

---

## Expected Result

Users should now be able to:

```text
1. Select Kubernetes cluster from dropdown
        ↓
2. Login with InsForge credentials
        ↓
3. Open Dashboard
        ↓
4. Click Investigate
        ↓
5. Watch real-time progress via WebSocket
        ↓
6. Receive AI-powered diagnosis
        ↓
7. View investigation history from PostgreSQL
        ↓
8. Handle errors gracefully
        ↓
9. See empty states when cluster is healthy
```

The system should now feel like:

> A production-ready AI-powered Kubernetes troubleshooting product.

---

## Testing Checklist

- [ ] Test CrashLoopBackOff detection
- [ ] Test ImagePullBackOff detection
- [ ] Test OOMKilled detection
- [ ] Test Service selector mismatch detection
- [ ] Test cluster selection feature
- [ ] Test authentication flow
- [ ] Test WebSocket real-time updates
- [ ] Test investigation history
- [ ] Test error handling (cluster unreachable)
- [ ] Test error handling (AI service down)
- [ ] Test empty state (healthy cluster)
- [ ] Test loading states
- [ ] Verify all logs are clean
- [ ] Verify no secrets in logs
- [ ] Test with multiple clusters
- [ ] Performance test with large clusters

---

## Best Practices

### Backend
- Use proper exception handling
- Log errors with context
- Validate all inputs
- Use connection pooling
- Implement health checks
- Add metrics for monitoring

### Frontend
- Handle all error states
- Show loading indicators
- Implement retry logic
- Use optimistic updates
- Cache API responses
- Handle network failures

### Security
- Never log sensitive data
- Validate JWT tokens
- Use HTTPS in production
- Implement rate limiting
- Add CORS properly
- Sanitize all inputs

### Performance
- Use database indexes
- Implement caching
- Optimize API calls
- Limit log collection
- Use pagination
- Monitor resource usage
