# Multi-stage build for Spring Boot application with Gradle

# Stage 1: Build
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build application
RUN gradle bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/build/libs/k8s-agent.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]