# Multi-stage Dockerfile for Ktor Application
# Stage 1: Build stage with Gradle
# Stage 2: Runtime stage with JRE only

# ============================================
# Build Stage
# ============================================
FROM gradle:8.11-jdk17 AS build

WORKDIR /app

# Copy Gradle wrapper and build files first (for layer caching)
COPY gradle gradle
COPY gradlew gradlew.bat ./
COPY settings.gradle.kts build.gradle.kts gradle.properties ./

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src src

# Build Fat JAR
RUN gradle buildFatJar --no-daemon

# ============================================
# Runtime Stage
# ============================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install curl for health checking (before switching to non-root user)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r ktor && useradd -r -g ktor ktor

# Copy JAR from build stage
COPY --from=build /app/build/libs/*-all.jar app.jar

# Change ownership to non-root user
RUN chown -R ktor:ktor /app

# Switch to non-root user
USER ktor

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Run application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
