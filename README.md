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

## Features (Planned)

- ✅ Health check endpoints
- ⏳ Kubernetes cluster investigation
- ⏳ Pod inspection and log collection
- ⏳ AI-powered root cause analysis
- ⏳ Fix recommendations
- ⏳ Investigation history
- ⏳ Real-time updates

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

## License

Apache License 2.0

## Contributing

Contributions are welcome! Please read the contributing guidelines before submitting PRs.
