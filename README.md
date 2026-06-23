## 🎯 Implementation Status

- ✅ **Prompt 01**: Project Setup - COMPLETE
- ✅ **Prompt 02**: Kubernetes Investigation Engine - COMPLETE & TESTED
- ✅ **Prompt 03**: AI Reasoning Engine - COMPLETE & TESTED
- ✅ **Prompt 04**: Authentication & Database - COMPLETE
- ⏳ **Prompt 05**: End-to-End Integration - PENDING

> **Note:** This project originally planned to use InsForge but was implemented with Spring Boot + PostgreSQL instead. See [IMPLEMENTATION_NOTES.md](./IMPLEMENTATION_NOTES.md) and [INSFORGE_CLEANUP_SUMMARY.md](./INSFORGE_CLEANUP_SUMMARY.md) for details.

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
LLM Reasoning (OpenRouter)
    ↓
Root Cause + Suggested Fix
```

## Tech Stack

### Backend
- **Spring Boot 3.3+** - Application framework
- **Java 21** - Programming language
- **Gradle 8.x** - Build tool
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database access
- **PostgreSQL** - Database
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
- **Zustand** - State management
- **React Router** - Routing

### Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **PostgreSQL 15** - Database

## Project Structure

```
AI-Kubernetes-Agent/
├── backend/                    # Spring Boot Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/k8s/agent/
│   │   │   │   ├── K8sAgentApplication.java
│   │   │   │   ├── config/         # Security, CORS, App config
│   │   │   │   ├── controller/     # REST endpoints
│   │   │   │   ├── service/        # Business logic
│   │   │   │   ├── entity/         # JPA entities
│   │   │   │   ├── repository/     # Data access
│   │   │   │   ├── dto/            # Data transfer objects
│   │   │   │   └── security/       # JWT filters
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   ├── build.gradle
│   ├── settings.gradle
│   └── Dockerfile
│
├── frontend/                   # React Application
│   ├── src/
│   │   ├── components/         # UI components
│   │   ├── services/           # API services
│   │   ├── store/              # State management
│   │   ├── types/              # TypeScript types
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   └── Dockerfile
│
├── docker-compose.yml
├── BUILD.md                    # Detailed build instructions
└── README.md
```

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Gradle 8.x** (or use included Gradle wrapper)
- **Node.js 18+** and npm
- **Docker** and Docker Compose
- **kubectl** configured with a Kubernetes cluster

### Quick Start

See [BUILD.md](./BUILD.md) for comprehensive setup instructions.

```bash
# 1. Start PostgreSQL
docker compose up -d postgres

# 2. Setup backend environment
cd backend
cp .env.example .env
# Edit .env with your credentials

# 3. Start backend
./gradlew bootRun

# 4. Setup frontend (in new terminal)
cd frontend
npm install
npm run dev

# 5. Access the application
# Frontend: http://localhost:5173
# Backend API: http://localhost:8080
```

## API Endpoints

### Authentication (Public)
```
POST /api/v1/auth/register    # Register new user
POST /api/v1/auth/login       # Login
GET  /api/v1/auth/validate    # Validate JWT token
```

### Health Check (Public)
```
GET /actuator/health          # Spring Boot health
GET /api/v1/investigate/health # Investigation service health
```

### Kubernetes Investigation (Protected - Requires JWT)
```
POST /api/v1/investigate              # Full cluster investigation
GET  /api/v1/investigate/history      # Get investigation history
GET  /api/v1/investigate/history/{id} # Get specific investigation
```

## Environment Variables

### Backend (.env)
```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/k8s_agent
DATABASE_USERNAME=k8s_user
DATABASE_PASSWORD=k8s_password

# JWT
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION=86400000

# OpenRouter AI
OPENROUTER_API_KEY=your-openrouter-api-key
OPENROUTER_MODEL=anthropic/claude-3.5-sonnet

# Kubernetes
KUBECONFIG_PATH=/path/to/your/kubeconfig
```

### Frontend (.env.local)
```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Features

### Completed
- ✅ Health check endpoints
- ✅ Kubernetes cluster investigation
- ✅ Pod inspection and failure detection
- ✅ Log collection from problematic pods
- ✅ Kubernetes events analysis
- ✅ Deployment status inspection
- ✅ Network and service validation
- ✅ AI-powered root cause analysis
- ✅ Fix recommendations using LLM
- ✅ User authentication (JWT)
- ✅ User registration and login
- ✅ Protected API endpoints
- ✅ PostgreSQL database integration
- ✅ Investigation history storage
- ✅ Frontend authentication UI
- ✅ Docker multi-stage builds
- ✅ Docker Compose orchestration

### Planned (Prompt 05)
- ⏳ Real-time investigation updates (WebSocket)
- ⏳ Investigation dashboard UI enhancements
- ⏳ Investigation result visualization
- ⏳ End-to-end testing

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

## Security

- **Authentication**: JWT-based stateless authentication
- **Password Hashing**: BCrypt with salt
- **CORS**: Configured for frontend origin
- **Protected Routes**: All investigation endpoints require authentication
- **Token Expiration**: Configurable (default 24 hours)

## Documentation

- [BUILD.md](./BUILD.md) - Comprehensive build and deployment guide
- [ENV_GUIDE.md](./ENV_GUIDE.md) - Environment variables guide
- [KUBERNETES_SETUP.md](./KUBERNETES_SETUP.md) - Kubernetes setup instructions

## License

Apache License 2.0

## Contributing

Contributions are welcome! Please read the contributing guidelines before submitting PRs.

---

**Built with ❤️ using Spring Boot, React, and PostgreSQL**
