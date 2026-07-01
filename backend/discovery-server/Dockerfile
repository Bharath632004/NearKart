# Stage 1 – Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 – Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S nearkart && adduser -S nearkart -G nearkart
USER nearkart

COPY --from=builder /app/target/discovery-server-*.jar app.jar

EXPOSE 8761

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", "app.jar"]
