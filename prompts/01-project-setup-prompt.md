# 01-prompt-project-setup.md

## Context

We are building an **AI Kubernetes Troubleshooting Agent**.

Architecture:

```text
React Frontend (TypeScript)
    ↓
Spring Boot Backend (Java)
    ↓
Kubernetes Investigation Layer
    ↓
AI Kubernetes Agent
    ↓
LLM Reasoning (OpenRouter via InsForge)
    ↓
Root Cause + Suggested Fix
    ↓
Frontend Diagnosis
```

This is an **on-demand troubleshooting system**.

Example flow:

```text
User clicks "Investigate Cluster"
        ↓
REST API call
        ↓
Kubernetes investigation
        ↓
AI reasoning
        ↓
Diagnosis shown to user
```

We are **NOT building a Kubernetes controller/operator**.

---

## Goal

Set up the project foundation.

Create:

- Spring Boot backend
- React frontend with TypeScript
- Docker setup
- Environment variables
- Basic folder structure
- Health endpoint

Do NOT implement Kubernetes logic or AI yet.

---

## Tech Stack

Backend:

- **Spring Boot 3.3+**
- **Java 21**
- **Maven 3.9+**
- **Spring Web** (REST APIs)
- **Spring Boot Actuator** (Health checks)
- **Lombok** (Reduce boilerplate)
- **Spring Boot DevTools** (Hot reload)
- **Logback** (Logging)

Frontend:

- **React 18+**
- **TypeScript 5+**
- **Vite** (Build tool)
- **Tailwind CSS**
- **Axios** (HTTP client)
- **TanStack Query (React Query)** (Data fetching)

Infrastructure:

- **Docker**
- **Docker Compose**
- **Maven** (Backend build)
- **npm/pnpm** (Frontend build)

---

## Project Structure

Create a monorepo:

```text
ai-kubernetes-agent/

├── backend/                          # Spring Boot Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/k8s/agent/
│   │   │   │       ├── K8sAgentApplication.java
│   │   │   │       ├── config/
│   │   │   │       │   └── CorsConfig.java
│   │   │   │       ├── controller/
│   │   │   │       │   └── HealthController.java
│   │   │   │       ├── service/
│   │   │   │       │   ├── investigation/
│   │   │   │       │   ├── ai/
│   │   │   │       │   └── kubernetes/
│   │   │   │       ├── model/
│   │   │   │       │   ├── dto/
│   │   │   │       │   └── entity/
│   │   │   │       └── repository/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── application-dev.yml
│   │   └── test/
│   │       └── java/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .gitignore
│
├── frontend/                         # React Application
│   ├── src/
│   │   ├── components/
│   │   │   └── Dashboard.tsx
│   │   ├── services/
│   │   │   └── api.ts
│   │   ├── hooks/
│   │   ├── types/
│   │   │   └── index.ts
│   │   ├── App.tsx
│   │   ├── main.tsx
│   │   └── index.css
│   ├── public/
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── Dockerfile
│   └── .gitignore
│
├── docs/
├── prompts/
├── docker-compose.yml
├── AGENTS.md
└── README.md
```

Backend should include placeholders for:

```text
service/investigation/
service/ai/
service/kubernetes/
model/dto/
model/entity/
repository/
```

Frontend should include placeholders for:

```text
components/
services/
hooks/
types/
```

Use placeholder implementations only.

Example Java:

```java
@Service
public class PodInspectorService {
    public void inspectPods() {
        // TODO: Implement in next prompt
    }
}
```

Example TypeScript:

```typescript
export const investigateCluster = async () => {
  // TODO: Implement API call
};
```

---

## Backend Requirements

Create Spring Boot application.

### Main Application Class

```java
@SpringBootApplication
public class K8sAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(K8sAgentApplication.class, args);
    }
}
```

### Health Controller

Add REST endpoint:

```text
GET /actuator/health
GET /api/v1/health
```

Response:

```json
{
  "status": "UP",
  "service": "ai-kubernetes-agent",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Configuration

Enable:

- CORS (allow frontend origin)
- Logging (Logback with JSON format)
- Environment variable loading
- Spring Boot Actuator

### Maven Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Keep code simple and modular.

---

## Frontend Requirements

Create a minimal homepage using React + TypeScript.

### UI Components

```text
AI Kubernetes Agent

Troubleshoot Kubernetes with AI

[ Investigate Cluster ]

System Status: Ready
```

### Tech Setup

- Use Vite for fast development
- Configure Tailwind CSS
- Setup TypeScript strict mode
- Add Axios for API calls
- Configure React Query

### Package.json Scripts

```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx"
  }
}
```

Simple professional styling only.

---

## Environment Variables

### Backend (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: ai-kubernetes-agent

# Placeholder for future configs
kubernetes:
  config-path: ${KUBECONFIG_PATH:~/.kube/config}

openrouter:
  api-key: ${OPENROUTER_API_KEY:}
  model: ${OPENROUTER_MODEL:anthropic/claude-3-sonnet}

insforge:
  api-key: ${INSFORGE_API_KEY:}
  api-base-url: ${INSFORGE_API_BASE_URL:}

logging:
  level:
    com.k8s.agent: INFO
```

### Frontend (.env)

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

---

## Docker Requirements

### Backend Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend Dockerfile

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose

```yaml
version: '3.8'

services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      - ~/.kube/config:/root/.kube/config:ro

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
    environment:
      - VITE_API_BASE_URL=http://localhost:8080
```

---

## Constraints

DO NOT implement:

- Kubernetes Java Client logic
- AI reasoning
- OpenRouter integration
- InsForge integration
- Authentication
- Realtime updates
- Database connections

Only setup the foundation.

DO NOT BREAK EXISTING CODE in future implementations.

Keep everything:
- Enterprise-grade
- Production-ready
- Well-structured
- Easy to extend

---

## Expected Result

I should be able to run:

```bash
# Using Docker Compose
docker compose up --build

# Or manually
cd backend && mvn spring-boot:run
cd frontend && npm run dev
```

Access:

```text
Frontend: http://localhost:3000
Backend Health: http://localhost:8080/actuator/health
Backend API: http://localhost:8080/api/v1/health
```

### Health Check Response

```json
{
  "status": "UP",
  "service": "ai-kubernetes-agent",
  "timestamp": "2024-01-01T00:00:00.000Z",
  "components": {
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## Best Practices

### Java/Spring Boot
- Use `@RestController` for REST endpoints
- Use `@Service` for business logic
- Use `@Configuration` for configs
- Use Lombok to reduce boilerplate (`@Data`, `@Slf4j`)
- Follow package-by-feature structure
- Use constructor injection (not field injection)

### React/TypeScript
- Use functional components with hooks
- Define proper TypeScript interfaces
- Use React Query for server state
- Keep components small and focused
- Use Tailwind utility classes

### General
- Follow SOLID principles
- Write clean, readable code
- Add meaningful comments
- Use consistent naming conventions
- Prepare for horizontal scaling
