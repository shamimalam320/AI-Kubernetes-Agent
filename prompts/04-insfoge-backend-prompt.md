# 04-prompt-insforge-integration.md

## Context

The backend can now:

```text
Investigate Kubernetes
        ↓
Collect Evidence
        ↓
AI Reasoning
        ↓
Root Cause Analysis
        ↓
Suggested Fix
```

Architecture:

```text
React Frontend
    ↓
Spring Boot REST API
    ↓
Kubernetes Investigation Layer
    ↓
AI Kubernetes Agent
    ↓
LLM Reasoning
(OpenRouter via InsForge Key)
    ↓
Root Cause + Suggested Fix
```

Goal:

We now want to turn this into a **real application experience**.

Users should be able to:

```text
Login
        ↓
Click Investigate
        ↓
See live investigation progress
        ↓
Receive diagnosis
        ↓
View investigation history
```

Use InsForge for:

```text
Authentication (Spring Security + JWT)
Investigation History (PostgreSQL via InsForge)
Realtime Updates (WebSocket)
```

---

## Goal

Build **InsForge Integration + Frontend Dashboard**.

Implement:

```text
Backend:
- Spring Security with InsForge JWT
- Investigation History (JPA + PostgreSQL)
- WebSocket for real-time updates
- InsForge SDK integration

Frontend:
- React Dashboard
- Authentication flow
- Real-time progress display
- Investigation history view
```

Keep implementation clean and enterprise-grade.

---

## Backend Requirements

### 1. InsForge SDK Integration

Add InsForge SDK dependency:

```xml
<dependency>
    <groupId>dev.insforge</groupId>
    <artifactId>insforge-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

Configuration (application.yml):

```yaml
insforge:
  api-key: ${INSFORGE_API_KEY}
  api-base-url: ${INSFORGE_API_BASE_URL}
  project-id: ${INSFORGE_PROJECT_ID}

spring:
  datasource:
    url: ${INSFORGE_DATABASE_URL}
    username: ${INSFORGE_DATABASE_USER}
    password: ${INSFORGE_DATABASE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

Create InsForge configuration:

```java
@Configuration
@Slf4j
public class InsForgeConfig {
    
    @Value("${insforge.api-key}")
    private String apiKey;
    
    @Value("${insforge.api-base-url}")
    private String apiBaseUrl;
    
    @Bean
    public InsForgeClient insForgeClient() {
        return InsForgeClient.builder()
            .apiKey(apiKey)
            .apiBaseUrl(apiBaseUrl)
            .build();
    }
}
```

---

### 2. Authentication (Spring Security + InsForge JWT)

Implement JWT-based authentication using InsForge.

Requirements:

- Validate JWT tokens from InsForge
- Protect investigation endpoints
- User session handling

Spring Security Configuration:

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) 
        throws Exception {
        
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health",
                    "/api/v1/auth/**",
                    "/ws/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(
                jwtAuthFilter, 
                UsernamePasswordAuthenticationFilter.class
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = 
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

JWT Authentication Filter:

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = authHeader.substring(7);
            String userId = jwtService.extractUserId(jwt);
            
            if (userId != null && SecurityContextHolder.getContext()
                .getAuthentication() == null) {
                
                if (jwtService.isTokenValid(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userId, 
                            null, 
                            List.of()
                        );
                    
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                            .buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed", e);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

JWT Service:

```java
@Service
@Slf4j
public class JwtService {
    
    @Value("${insforge.jwt-secret}")
    private String jwtSecret;
    
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private <T> T extractClaim(
        String token, 
        Function<Claims, T> claimsResolver
    ) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

Keep auth implementation minimal and clean.

---

### 3. Investigation History (JPA + PostgreSQL)

Store investigation history in InsForge PostgreSQL database.

Entity:

```java
@Entity
@Table(name = "investigations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Investigation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String rootCause;
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @Column(columnDefinition = "TEXT")
    private String suggestedFix;
    
    @Column(nullable = false)
    private Integer confidence;
    
    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, IN_PROGRESS
    
    @Column(columnDefinition = "JSONB")
    private String investigationData; // Full investigation result as JSON
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

Repository:

```java
@Repository
public interface InvestigationRepository 
    extends JpaRepository<Investigation, UUID> {
    
    List<Investigation> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Investigation> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
    
    Optional<Investigation> findByIdAndUserId(UUID id, String userId);
}
```

Service:

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class InvestigationHistoryService {
    
    private final InvestigationRepository repository;
    private final ObjectMapper objectMapper;
    
    public Investigation saveInvestigation(
        String userId,
        InvestigationResponse response
    ) {
        try {
            String investigationJson = objectMapper.writeValueAsString(response);
            
            Investigation investigation = Investigation.builder()
                .userId(userId)
                .rootCause(response.getDiagnosis().getRootCause())
                .explanation(response.getDiagnosis().getExplanation())
                .suggestedFix(response.getDiagnosis().getSuggestedFix())
                .confidence(response.getDiagnosis().getConfidence())
                .status("SUCCESS")
                .investigationData(investigationJson)
                .build();
            
            return repository.save(investigation);
            
        } catch (Exception e) {
            log.error("Failed to save investigation", e);
            throw new RuntimeException("Failed to save investigation", e);
        }
    }
    
    public List<Investigation> getUserInvestigations(String userId) {
        return repository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Optional<Investigation> getInvestigation(UUID id, String userId) {
        return repository.findByIdAndUserId(id, userId);
    }
}
```

---

### 4. Real-time Updates (WebSocket)

Implement WebSocket for live investigation progress.

WebSocket Configuration:

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("http://localhost:3000")
            .withSockJS();
    }
}
```

Progress Service:

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class InvestigationProgressService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void sendProgress(String userId, String step, String status) {
        ProgressUpdate update = ProgressUpdate.builder()
            .step(step)
            .status(status)
            .timestamp(LocalDateTime.now())
            .build();
        
        messagingTemplate.convertAndSendToUser(
            userId,
            "/topic/investigation-progress",
            update
        );
        
        log.info("Progress sent: {} - {}", step, status);
    }
}

@Data
@Builder
class ProgressUpdate {
    private String step;
    private String status;
    private LocalDateTime timestamp;
}
```

Update InvestigationService to send progress:

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class InvestigationService {
    
    private final InvestigationProgressService progressService;
    // ... other dependencies
    
    public InvestigationResult investigate(String userId) {
        progressService.sendProgress(userId, "Checking Pods", "IN_PROGRESS");
        PodInspectionResult pods = podInspectorService.inspectPods();
        progressService.sendProgress(userId, "Checking Pods", "COMPLETED");
        
        progressService.sendProgress(userId, "Collecting Logs", "IN_PROGRESS");
        LogsCollectionResult logs = logsCollectorService.collectLogs(
            pods.getProblematicPods()
        );
        progressService.sendProgress(userId, "Collecting Logs", "COMPLETED");
        
        // ... continue for other steps
        
        return result;
    }
}
```

---

### 5. Updated Investigation Controller

Update controller to include authentication and history:

```java
@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class InvestigationController {
    
    private final InvestigationService investigationService;
    private final AIService aiService;
    private final InvestigationHistoryService historyService;
    
    @PostMapping("/investigate")
    public ResponseEntity<ApiResponse<InvestigationResponse>> investigate(
        @AuthenticationPrincipal String userId
    ) {
        log.info("Investigation request from user: {}", userId);
        
        try {
            // 1. Collect Kubernetes evidence
            InvestigationResult investigation = 
                investigationService.investigate(userId);
            
            // 2. AI diagnosis
            DiagnosisResult diagnosis = aiService.diagnose(investigation);
            
            // 3. Build response
            InvestigationResponse response = InvestigationResponse.builder()
                .investigation(investigation)
                .diagnosis(diagnosis)
                .build();
            
            // 4. Save to history
            historyService.saveInvestigation(userId, response);
            
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
    
    @GetMapping("/investigations")
    public ResponseEntity<ApiResponse<List<Investigation>>> getHistory(
        @AuthenticationPrincipal String userId
    ) {
        List<Investigation> history = historyService.getUserInvestigations(userId);
        
        return ResponseEntity.ok(
            ApiResponse.<List<Investigation>>builder()
                .status("success")
                .data(history)
                .message("History retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
    
    @GetMapping("/investigations/{id}")
    public ResponseEntity<ApiResponse<Investigation>> getInvestigation(
        @PathVariable UUID id,
        @AuthenticationPrincipal String userId
    ) {
        Optional<Investigation> investigation = 
            historyService.getInvestigation(id, userId);
        
        if (investigation.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(
            ApiResponse.<Investigation>builder()
                .status("success")
                .data(investigation.get())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
```

---

## Frontend Requirements

### 1. React Dashboard Setup

Project structure:

```
frontend/src/
├── components/
│   ├── Dashboard.tsx
│   ├── InvestigationProgress.tsx
│   ├── DiagnosisCard.tsx
│   ├── HistoryList.tsx
│   └── LoginForm.tsx
├── services/
│   ├── api.ts
│   ├── auth.ts
│   └── websocket.ts
├── hooks/
│   ├── useInvestigation.ts
│   └── useAuth.ts
├── types/
│   └── index.ts
├── store/
│   └── authStore.ts
├── App.tsx
└── main.tsx
```

---

### 2. Authentication Flow

Auth Service (services/auth.ts):

```typescript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: {
    id: string;
    email: string;
  };
}

export const authService = {
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await axios.post(
      `${API_BASE_URL}/api/v1/auth/login`,
      credentials
    );
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('token');
  },

  getToken: (): string | null => {
    return localStorage.getItem('token');
  },

  setToken: (token: string) => {
    localStorage.setItem('token', token);
  },
};
```

Auth Store (store/authStore.ts):

```typescript
import { create } from 'zustand';

interface AuthState {
  token: string | null;
  userId: string | null;
  isAuthenticated: boolean;
  login: (token: string, userId: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('token'),
  userId: localStorage.getItem('userId'),
  isAuthenticated: !!localStorage.getItem('token'),
  
  login: (token, userId) => {
    localStorage.setItem('token', token);
    localStorage.setItem('userId', userId);
    set({ token, userId, isAuthenticated: true });
  },
  
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    set({ token: null, userId: null, isAuthenticated: false });
  },
}));
```

---

### 3. Investigation Dashboard Component

Dashboard.tsx:

```typescript
import React, { useState } from 'react';
import { useInvestigation } from '../hooks/useInvestigation';
import InvestigationProgress from './InvestigationProgress';
import DiagnosisCard from './DiagnosisCard';
import HistoryList from './HistoryList';

const Dashboard: React.FC = () => {
  const { investigate, isLoading, diagnosis, error } = useInvestigation();
  const [progress, setProgress] = useState<string[]>([]);

  const handleInvestigate = async () => {
    setProgress([]);
    await investigate((step) => {
      setProgress((prev) => [...prev, step]);
    });
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-6xl mx-auto">
        <h1 className="text-4xl font-bold text-gray-900 mb-8">
          AI Kubernetes Agent
        </h1>

        <div className="bg-white rounded-lg shadow p-6 mb-8">
          <button
            onClick={handleInvestigate}
            disabled={isLoading}
            className="w-full bg-blue-600 text-white py-3 px-6 rounded-lg
                     hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed
                     font-semibold text-lg transition-colors"
          >
            {isLoading ? 'Investigating...' : 'Investigate Cluster'}
          </button>
        </div>

        {isLoading && <InvestigationProgress steps={progress} />}

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-8">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        {diagnosis && <DiagnosisCard diagnosis={diagnosis} />}

        <HistoryList />
      </div>
    </div>
  );
};

export default Dashboard;
```

---

### 4. Real-time Progress Component

InvestigationProgress.tsx:

```typescript
import React from 'react';

interface Props {
  steps: string[];
}

const InvestigationProgress: React.FC<Props> = ({ steps }) => {
  const allSteps = [
    'Checking Pods',
    'Collecting Logs',
    'Analyzing Events',
    'Inspecting Deployments',
    'Checking Network',
    'AI Reasoning',
    'Root Cause Found',
  ];

  return (
    <div className="bg-white rounded-lg shadow p-6 mb-8">
      <h2 className="text-xl font-semibold mb-4">Investigation Progress</h2>
      <div className="space-y-2">
        {allSteps.map((step) => (
          <div key={step} className="flex items-center">
            {steps.includes(step) ? (
              <span className="text-green-500 mr-2">✓</span>
            ) : (
              <span className="text-gray-300 mr-2">○</span>
            )}
            <span className={steps.includes(step) ? 'text-gray-900' : 'text-gray-400'}>
              {step}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default InvestigationProgress;
```

---

### 5. WebSocket Hook

hooks/useInvestigation.ts:

```typescript
import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiService } from '../services/api';
import { websocketService } from '../services/websocket';

export const useInvestigation = () => {
  const [diagnosis, setDiagnosis] = useState(null);

  const { mutate: investigate, isLoading, error } = useMutation({
    mutationFn: async (onProgress: (step: string) => void) => {
      // Connect to WebSocket
      websocketService.connect((progress) => {
        onProgress(progress.step);
      });

      // Trigger investigation
      const result = await apiService.investigate();
      return result;
    },
    onSuccess: (data) => {
      setDiagnosis(data.diagnosis);
      websocketService.disconnect();
    },
    onError: () => {
      websocketService.disconnect();
    },
  });

  return { investigate, isLoading, diagnosis, error };
};
```

---

## Maven Dependencies

Add to pom.xml:

```xml
<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
</dependencies>
```

---

## Constraints

DO NOT change working backend investigation logic.

DO NOT change AI reasoning flow.

DO NOT overengineer UI.

DO NOT add charts.

Use InsForge for:

```text
Authentication (JWT validation)
Database (PostgreSQL)
Real-time updates (WebSocket)
```

Spring Boot must remain the orchestrator.

DO NOT BREAK EXISTING CODE.

Only extend functionality.

---

## Expected Result

Users should now be able to:

```text
Login with InsForge credentials
        ↓
Open Dashboard
        ↓
Click Investigate
        ↓
Watch real-time progress via WebSocket
        ↓
Receive diagnosis
        ↓
View investigation history from PostgreSQL
```

The system should now feel like:

> A real AI-powered Kubernetes troubleshooting product.

---

## Best Practices

### Backend
- Use Spring Security for authentication
- Validate JWT tokens properly
- Use JPA for database operations
- Send WebSocket updates for progress
- Handle errors gracefully

### Frontend
- Use React Query for API calls
- Implement WebSocket for real-time updates
- Keep UI simple and professional
- Handle loading and error states
- Use TypeScript for type safety

### Security
- Never expose JWT secrets
- Validate all user inputs
- Use HTTPS in production
- Implement rate limiting
- Add CORS properly
