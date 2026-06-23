## 🎯 Implementation Status

- ✅ **Prompt 01**: Project Setup - COMPLETE
- ✅ **Prompt 02**: Kubernetes Investigation Engine - COMPLETE & TESTED
- ✅ **Prompt 03**: AI Reasoning Engine - COMPLETE & TESTED
- ⏳ **Prompt 04**: InsForge Backend Integration - PENDING
- ⏳ **Prompt 05**: End-to-End Integration - PENDING

# AI Kubernetes Troubleshooting Agent

An AI-powered platform for troubleshooting Kubernetes clusters using intelligent analysis of pods, logs, events, and cluster state.

## Architecture

```
React Frontend (TypeScript)
    ↓
Spring Boot Backend (Java 21)
    ↓
Kubernetes Investigation Layer
    ↓
AI Kubernetes Agent
    ↓
LLM Reasoning (OpenRouter via InsForge)
    ↓
Root Cause + Suggested Fix
```

## Tech Stack

### Backend
- **Spring Boot 3.3+** - Application framework
- **Java 21** - Programming language
- **Gradle 8.x** - Build tool
- **Spring Web** - REST APIs
- **Spring Boot Actuator** - Health checks
- **Lombok** - Reduce boilerplate
- **Kubernetes Java Client 19.0.0** - Kubernetes API integration

### Frontend
- **React 18+** - UI framework
- **TypeScript 5+** - Type safety
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **Axios** - HTTP client
- **TanStack Query** - Data fetching

### Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

## Project Structure

```
AI-Kubernetes-Agent/
├── backend/                    # Spring Boot Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/k8s/agent/
│   │   │   │   ├── K8sAgentApplication.java
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── model/
│   │   │   │   └── repository/
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   ├── build.gradle
│   ├── settings.gradle
│   └── Dockerfile
│
├── frontend/                   # React Application
│   ├── src/
│   │   ├── components/
│   │   ├── services/
│   │   ├── hooks/
│   │   ├── types/
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   └── Dockerfile
│
├── docker-compose.yml
└── README.md
```

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Gradle 8.x** (or use included Gradle wrapper)
- **Node.js 18+** and npm
- **Docker** and Docker Compose (optional)

### Option 1: Using Docker Compose (Recommended)

```bash
# Build and start all services
docker compose up --build

# Access the application
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080/api/v1/health
# Backend Health: http://localhost:8080/actuator/health
```

### Option 2: Manual Setup

#### Backend

```bash
cd backend

# Run with Gradle wrapper
./gradlew bootRun

# Or build JAR and run
./gradlew bootJar
java -jar build/libs/*.jar

# Or use system Gradle
gradle bootRun
```

Backend will be available at `http://localhost:8080`

#### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will be available at `http://localhost:5173`

## API Endpoints

### Health Check
```
GET /api/v1/health
GET /actuator/health
```

Response:
```json
{
  "status": "UP",
  "service": "ai-kubernetes-agent",
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

### Kubernetes Investigation

#### Comprehensive Investigation
```
POST /api/v1/investigate
```

Performs full cluster investigation including:
- Pod health inspection
- Log collection from problematic pods
- Kubernetes events analysis
- Deployment status checks
- Network and service validation

Response:
```json
{
  "success": true,
  "message": "Cluster investigation complete",
  "data": {
    "timestamp": "2024-01-15T10:30:00",
    "clusterHealthy": false,
    "investigationDurationSeconds": 12,
    "podInspection": { ... },
    "logsCollection": { ... },
    "eventsAnalysis": { ... },
    "deploymentInspection": { ... },
    "networkInspection": { ... }
  }
}
```

#### Quick Health Check
```
GET /api/v1/investigate/quick
```

Performs quick health check (pods and events only) for faster response.

#### Investigation Service Health
```
GET /api/v1/investigate/health
```

Check if the investigation service is operational.

**See [KUBERNETES_SETUP.md](./KUBERNETES_SETUP.md) for detailed setup and usage instructions.**

## Development

### Backend Development

```bash
cd backend

# Run tests
./gradlew test

# Run with hot reload
./gradlew bootRun

# Build JAR
./gradlew bootJar

# Clean build
./gradlew clean build
```

### Frontend Development

```bash
cd frontend

# Run development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

## Environment Variables

### Backend (.env or application.yml)
```yaml
KUBECONFIG_PATH=~/.kube/config
OPENROUTER_API_KEY=your-api-key
OPENROUTER_MODEL=anthropic/claude-3-sonnet
INSFORGE_API_KEY=your-insforge-key
INSFORGE_API_BASE_URL=https://your-project.insforge.app
```

### Frontend (.env)
```
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

## Features

### Completed (Prompt 01 & 02)
- ✅ Health check endpoints
- ✅ Kubernetes cluster investigation
- ✅ Pod inspection and failure detection
- ✅ Log collection from problematic pods
- ✅ Kubernetes events analysis
- ✅ Deployment status inspection
- ✅ Network and service validation
- ✅ Comprehensive investigation orchestration
- ✅ REST API endpoints for investigation
- ✅ Frontend TypeScript types
- ✅ Docker multi-stage builds
- ✅ Docker Compose orchestration

### Planned (Prompt 03+)
- ⏳ AI-powered root cause analysis
- ⏳ Fix recommendations using LLM
- ⏳ Investigation history storage
- ⏳ Real-time investigation updates
- ⏳ InsForge backend integration
- ⏳ User authentication
- ⏳ Investigation dashboard UI

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

## Implementation Progress

### ✅ Prompt 01: Project Setup (Completed)
- Spring Boot 3.3.0 backend with Java 21
- React 18 frontend with TypeScript and Vite
- Docker multi-stage builds
- Docker Compose orchestration
- Health check endpoints
- CORS configuration

### ✅ Prompt 02: Kubernetes Investigation Engine (Completed)
- **Kubernetes Java Client Integration** - Official client library v19.0.0
- **Pod Inspector Service** - Detects CrashLoopBackOff, ImagePullBackOff, OOMKilled, Pending pods
- **Logs Collector Service** - Collects and filters logs for error patterns
- **Events Analyzer Service** - Analyzes Kubernetes events for critical issues
- **Deployment Inspector Service** - Checks deployment health and replica status
- **Network Inspector Service** - Validates services and endpoints
- **Investigation Orchestrator** - Coordinates all investigation components
- **REST API Endpoints** - Full and quick investigation endpoints
- **Global Exception Handler** - Centralized error handling
- **Frontend Types** - Complete TypeScript definitions
- **Configuration** - Kubernetes settings in application.yml
- **Documentation** - Comprehensive setup guide (KUBERNETES_SETUP.md)

### ⏳ Prompt 03: AI Reasoning Engine (Pending)
- LLM integration via OpenRouter
- Root cause analysis
- Fix recommendations
- Confidence scoring

### ⏳ Prompt 04: InsForge Backend (Pending)
- Authentication
- Investigation history storage
- Real-time updates
- API layer

### ⏳ Prompt 05: End-to-End Integration (Pending)
- Frontend dashboard
- Investigation UI
- Real-time progress
- Deployment

## Documentation

- **[BUILD.md](./BUILD.md)** - Build and deployment instructions
- **[KUBERNETES_SETUP.md](./KUBERNETES_SETUP.md)** - Kubernetes investigation setup guide
- **[README.md](./README.md)** - This file

## License

Apache License 2.0

## Contributing

Contributions are welcome! Please read the contributing guidelines before submitting PRs.
