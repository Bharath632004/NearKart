# ─── Stage 1: Build ───────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom first (layer cache: dependencies)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for security best practice
RUN addgroup -S nearkart && adduser -S nearkart -G nearkart
USER nearkart

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8761

# Health check so Docker/Compose knows when the server is ready
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
