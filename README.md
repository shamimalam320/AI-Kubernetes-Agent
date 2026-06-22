# AI Kubernetes Troubleshooting Agent - High Level Design (HLD)

## Goal

Build an AI-powered Kubernetes troubleshooting platform that can:

- Investigate Kubernetes failures
- Analyze logs, events, and cluster state
- Identify root causes
- Suggest fixes
- Store investigation history
- Be deployed publicly as a real application

---

## Technology Stack

### Backend
- **Spring Boot 3.3+** (Java 21)
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database operations
- **Spring WebFlux** - Real-time updates
- **Kubernetes Java Client** - K8s cluster interaction
- **Maven** - Build tool
- **Lombok** - Reduce boilerplate code

### Frontend
- **React 18+** with TypeScript
- **Vite** - Build tool & dev server
- **TanStack Query (React Query)** - Data fetching & caching
- **Tailwind CSS** - Styling
- **Zustand** - State management
- **Axios** - HTTP client
- **WebSocket** - Real-time updates

### Backend Platform
- **InsForge** - All-in-one backend platform
  - PostgreSQL Database
  - Authentication
  - File Storage
  - Real-time capabilities
  - AI Gateway (OpenRouter)
  - Edge Functions

### Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Local development
- **Kubernetes** - Production deployment
- **Maven** - Dependency management

---

# High Level Architecture

```text
┌────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                     │
│                                                            │
│  Pods | Deployments | Services | Events | Logs            │
│                                                            │
│  This is where failures happen and evidence exists         │
└────────────────────────────────────────────────────────────┘
                              │
                              │ Kubernetes Java Client API
                              ▼
┌────────────────────────────────────────────────────────────┐
│                  Investigation Layer                      │
│                     (Spring Boot Services)                │
│                                                            │
│ Responsibility:                                            │
│ - Connect to Kubernetes cluster                            │
│ - Collect troubleshooting signals                          │
│ - Gather debugging evidence                                │
│                                                            │
│ Components:                                                │
│                                                            │
│  1. PodInspectorService                                    │
│     - Get pod health                                       │
│     - Detect CrashLoopBackOff                              │
│     - Detect Pending/Error states                          │
│                                                            │
│  2. LogsCollectorService                                   │
│     - Read pod logs                                        │
│     - Capture container errors                             │
│                                                            │
│  3. EventsAnalyzerService                                  │
│     - Read Kubernetes events                               │
│     - Detect scheduling/image failures                     │
│                                                            │
│  4. DeploymentInspectorService                             │
│     - Inspect deployment status                            │
│     - Verify rollout health                                │
│                                                            │
│  5. NetworkInspectorService                                │
│     - Check services                                       │
│     - Validate selectors                                   │
│     - Investigate DNS/networking issues                    │
└────────────────────────────────────────────────────────────┘
                              │
                              │ Structured Investigation Data
                              ▼
┌────────────────────────────────────────────────────────────┐
│                  AI Kubernetes Agent                      │
│                   (Spring Boot Service)                   │
│                                                            │
│ Responsibility:                                            │
│ - Understand Kubernetes failures                           │
│ - Correlate logs + events + deployment state               │
│ - Identify root cause                                      │
│ - Recommend fixes                                          │
│                                                            │
│ Components:                                                │
│                                                            │
│  1. PromptBuilderService                                   │
│     - Convert investigation data into LLM prompt           │
│                                                            │
│  2. LLM Reasoning Layer (AIService)                        │
│     - Uses OpenRouter API Key from InsForge                │
│     - Supports models like:                                │
│       - Claude                                              │
│       - GPT                                                 │
│       - DeepSeek                                            │
│                                                            │
│  3. RootCauseAnalyzerService                               │
│     - Detect primary issue                                 │
│     - Correlate signals                                    │
│                                                            │
│  4. FixRecommendationService                               │
│     - Suggest kubectl fixes                                │
│     - Recommend YAML updates                               │
│                                                            │
│  5. ConfidenceEngineService                                │
│     - Confidence % for diagnosis                           │
└────────────────────────────────────────────────────────────┘
                              │
                              │ Investigation Result
                              ▼
┌────────────────────────────────────────────────────────────┐
│                    InsForge Backend                       │
│                                                            │
│ Responsibility:                                            │
│ - Authentication (Spring Security integration)             │
│ - Backend APIs                                             │
│ - Investigation history (PostgreSQL)                       │
│ - Realtime investigation updates (WebSocket)               │
│                                                            │
│ Components:                                                │
│                                                            │
│  1. Authentication                                         │
│     - User login via InsForge                              │
│     - JWT token validation                                 │
│                                                            │
│  2. REST API Layer                                         │
│     - Trigger investigations                               │
│     - Return AI analysis                                   │
│                                                            │
│  3. Investigation History (JPA Repository)                 │
│     - Store previous incidents                             │
│     - Save root cause reports                              │
│                                                            │
│  4. Realtime Updates (WebSocket)                           │
│     - Live investigation progress                          │
│                                                            │
│ Example:                                                    │
│  ✓ Checking pods                                           │
│  ✓ Reading logs                                            │
│  ✓ Analyzing events                                        │
│  ✓ Finding root cause                                      │
└────────────────────────────────────────────────────────────┘
                              │
                              │ REST API Response
                              ▼
┌────────────────────────────────────────────────────────────┐
│                     Frontend Dashboard                    │
│                   (React + TypeScript)                    │
│                                                            │
│ Responsibility:                                            │
│ - Trigger investigation                                    │
│ - Show realtime progress (WebSocket)                       │
│ - Display root cause                                       │
│ - Show suggested fixes                                     │
│ - Show investigation history                               │
│                                                            │
│ Example UI:                                                 │
│                                                            │
│ Incident: Payment Service Failure                          │
│                                                            │
│ Status: Investigating...                                   │
│                                                            │
│ ✓ Pods Checked                                             │
│ ✓ Events Analyzed                                          │
│ ✓ Logs Processed                                           │
│                                                            │
│ Root Cause: ImagePullBackOff                               │
│                                                            │
│ Suggested Fix:                                             │
│ Update invalid image tag                                   │
└────────────────────────────────────────────────────────────┘
                              │
                              │ Deploy Entire App
                              ▼
┌────────────────────────────────────────────────────────────┐
│                     InsForge Deployment                   │
│                                                            │
│ Responsibility:                                            │
│ - Deploy frontend (React build)                            │
│ - Deploy backend (Spring Boot JAR)                         │
│ - Generate public URL                                      │
│                                                            │
│ Output:                                                     │
│                                                            │
│ https://ai-k8s-agent.public-url.app                        │
│                                                            │
│ Enables public access to the troubleshooting platform      │
└────────────────────────────────────────────────────────────┘
```

# End-to-End Workflow

```text
User clicks "Investigate Cluster"
                │
                ▼
React Frontend sends API request
                │
                ▼
Spring Boot REST Controller
      (Orchestration Layer)
                │
                ├── Authenticate User (Spring Security + InsForge)
                │
                ▼
Investigation Layer (Spring Services)
                │
                ├── Check Pods (PodInspectorService)
                ├── Read Logs (LogsCollectorService)
                ├── Analyze Events (EventsAnalyzerService)
                ├── Inspect Deployments (DeploymentInspectorService)
                └── Check Networking (NetworkInspectorService)
                │
                ▼
AI Kubernetes Agent (AIService)
                │
                ▼
LLM Reasoning
      (OpenRouter via InsForge Key)
                │
                ▼
Root Cause Analysis
                │
                ▼
Suggested Fix Generated
                │
                ├── Save Investigation History
                │        (Spring Data JPA + InsForge PostgreSQL)
                │
                ├── Realtime Progress Updates
                │        (Spring WebFlux + WebSocket)
                │
                ▼
Frontend Receives Result (React Query)
                │
                ▼
User sees Diagnosis
```

# Example Failure Flow

```text
Issue:
Payment service unavailable

Agent Investigation:

✓ Pod Status Checked
✓ Logs Collected
✓ Events Analyzed

Detected Problem:
CrashLoopBackOff

Root Cause:
DATABASE_URL environment variable missing

Confidence:
94%

Suggested Fix:
Update deployment.yaml and add secret reference

Prevention:
Add startup validation checks
```

## Supported Kubernetes Problems

- CrashLoopBackOff
- ImagePullBackOff
- OOMKilled
- Pending Pods
- Resource Exhaustion
- Deployment Rollout Failures
- Service Selector Mismatch
- DNS Resolution Problems
- Readiness/Liveness Probe Failures
- Networking Issues

---

## Project Structure

```
ai-kubernetes-agent/
├── backend/                          # Spring Boot Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/k8s/agent/
│   │   │   │       ├── config/       # Spring configurations
│   │   │   │       ├── controller/   # REST controllers
│   │   │   │       ├── service/      # Business logic
│   │   │   │       │   ├── investigation/
│   │   │   │       │   ├── ai/
│   │   │   │       │   └── kubernetes/
│   │   │   │       ├── model/        # DTOs & Entities
│   │   │   │       ├── repository/   # JPA repositories
│   │   │   │       └── security/     # Security config
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── application-prod.yml
│   │   └── test/
│   ├── pom.xml                       # Maven dependencies
│   └── Dockerfile
│
├── frontend/                         # React Application
│   ├── src/
│   │   ├── components/               # React components
│   │   ├── services/                 # API services
│   │   ├── hooks/                    # Custom hooks
│   │   ├── types/                    # TypeScript types
│   │   ├── store/                    # Zustand store
│   │   └── App.tsx
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   └── Dockerfile
│
├── docker-compose.yml
├── AGENTS.md                         # InsForge integration guide
└── README.md
```

---

## Development Setup

### Prerequisites
- Java 21
- Node.js 18+
- Maven 3.8+
- Docker & Docker Compose
- kubectl configured
- InsForge account

### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

### Docker Setup
```bash
docker-compose up --build
```

---

## API Endpoints

### Investigation
- `POST /api/v1/investigate` - Trigger cluster investigation
- `GET /api/v1/investigations` - Get investigation history
- `GET /api/v1/investigations/{id}` - Get specific investigation
- `WS /ws/investigation` - Real-time investigation updates

### Health
- `GET /actuator/health` - Application health check
- `GET /actuator/info` - Application info

---

## Environment Variables

### Backend (application.yml)
```yaml
spring:
  datasource:
    url: ${INSFORGE_DATABASE_URL}
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${INSFORGE_AUTH_URL}

kubernetes:
  config-path: ${KUBECONFIG_PATH}

openrouter:
  api-key: ${OPENROUTER_API_KEY}
  model: ${OPENROUTER_MODEL:anthropic/claude-3-sonnet}

insforge:
  api-key: ${INSFORGE_API_KEY}
  api-base-url: ${INSFORGE_API_BASE_URL}
```

### Frontend (.env)
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
VITE_INSFORGE_URL=https://3gbwe6ph.ap-southeast.insforge.app
```

---

## Enterprise Features

### Security
- Spring Security with JWT authentication
- Role-based access control (RBAC)
- CORS configuration
- Rate limiting
- Input validation

### Monitoring
- Spring Boot Actuator
- Prometheus metrics
- Health checks
- Custom metrics for investigations

### Scalability
- Stateless REST API
- Horizontal scaling ready
- Connection pooling
- Async processing

### Reliability
- Exception handling
- Retry mechanisms
- Circuit breakers (Resilience4j)
- Graceful degradation

---

## Why Spring Boot + React?

### Enterprise-Grade
- ✅ IBM's preferred technology stack
- ✅ Battle-tested in production
- ✅ Strong security features
- ✅ Excellent Kubernetes integration

### Developer Experience
- ✅ Large talent pool
- ✅ Comprehensive documentation
- ✅ Rich ecosystem
- ✅ Strong IDE support (IntelliJ IDEA)

### Performance
- ✅ High throughput
- ✅ Low latency
- ✅ Efficient resource usage
- ✅ Production-ready

### Maintainability
- ✅ Clean architecture
- ✅ Type safety (Java + TypeScript)
- ✅ Easy testing
- ✅ Clear separation of concerns
