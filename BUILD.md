# Build Instructions

## Prerequisites

- Java 21
- Gradle 8.7+ (compatible with Spring Boot 3.3.0)
- Node.js 18+
- Docker & Docker Compose

## Quick Start with Docker Compose

```bash
# Build and start all services
docker compose up --build

# Access:
# Frontend: http://localhost:3000
# Backend: http://localhost:8080/api/v1/health
```

## Manual Build

### Backend (Spring Boot with Gradle 8.7)

```bash
cd backend

# Initialize Gradle wrapper (first time only)
gradle wrapper --gradle-version 8.7

# Build
./gradlew clean build

# Run
./gradlew bootRun

# Or run JAR directly
java -jar build/libs/k8s-agent.jar
```

### Frontend (React with Vite)

```bash
cd frontend

# Install dependencies
npm install

# Development
npm run dev

# Production build
npm run build

# Preview production build
npm run preview
```

## Docker Build

### Backend
```bash
cd backend
docker build -t k8s-agent-backend .
docker run -p 8080:8080 k8s-agent-backend
```

### Frontend
```bash
cd frontend
docker build -t k8s-agent-frontend .
docker run -p 3000:80 k8s-agent-frontend
```

## Environment Variables

### Backend
Set in `backend/src/main/resources/application.yml` or via environment:
- `KUBECONFIG_PATH` - Path to kubeconfig file
- `OPENROUTER_API_KEY` - OpenRouter API key
- `INSFORGE_API_KEY` - InsForge API key

### Frontend
Set in `frontend/.env`:
- `VITE_API_BASE_URL` - Backend API URL (default: http://localhost:8080)

## Health Check

```bash
# Backend health
curl http://localhost:8080/api/v1/health
curl http://localhost:8080/actuator/health

# Expected response
{"status":"UP","service":"ai-kubernetes-agent","timestamp":"..."}
```

## Production Deployment

```bash
# Build optimized images
docker compose build

# Start in production mode
docker compose up -d

# View logs
docker compose logs -f

# Stop services
docker compose down
```

## Troubleshooting

### Gradle Build Issues
If you encounter Gradle compatibility issues:
- Ensure you're using Gradle 8.7 (compatible with Spring Boot 3.3.0)
- Docker build uses `gradle:8.7-jdk21-alpine` image
- Clear Gradle cache: `./gradlew clean --no-daemon`

### Port Conflicts
If ports 3000 or 8080 are in use:
```bash
# Check what's using the port
netstat -ano | findstr :8080
netstat -ano | findstr :3000

# Modify docker-compose.yml to use different ports
```

### Docker Build Fails
```bash
# Clean Docker cache
docker system prune -a

# Rebuild without cache
docker compose build --no-cache
```