# Stage 1 – Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 – Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN addgroup --system nearkart && adduser --system --ingroup nearkart nearkart
USER nearkart

COPY --from=builder /app/target/order-service-*.jar app.jar

EXPOSE 8086

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", "app.jar"]
