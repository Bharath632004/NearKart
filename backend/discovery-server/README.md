# NearKart Discovery Server

Eureka Service Registry for the NearKart microservices architecture.

## Overview

All NearKart microservices register with this Discovery Server at startup. Other services query it to find the network location of services they depend on — eliminating hardcoded URLs.

```
┌─────────────────────────────────────────────────────────┐
│                  Discovery Server :8761                  │
│                  (Eureka Registry)                       │
├─────────────────────────────────────────────────────────┤
│  api-gateway  │  product-service  │  user-service  │ …  │
└─────────────────────────────────────────────────────────┘
```

## Quick Start

### Run Locally
```bash
cd backend/discovery-server
./mvnw spring-boot:run
```
Dashboard → http://localhost:8761  
Credentials: `admin` / `admin123`

### Run with Docker
```bash
docker build -t nearkart/discovery-server .
docker run -p 8761:8761 nearkart/discovery-server
```

### Run via Docker Compose (from project root)
```bash
docker-compose up discovery-server
```

## Configuration

| Property | Default | Env Variable | Description |
|---|---|---|---|
| `server.port` | `8761` | `SERVER_PORT` | HTTP port |
| `eureka.security.username` | `admin` | `EUREKA_USERNAME` | Dashboard username |
| `eureka.security.password` | `admin123` | `EUREKA_PASSWORD` | Dashboard password |
| `eureka.instance.hostname` | `localhost` | `EUREKA_HOSTNAME` | Hostname for peer URLs |
| `eureka.server.enable-self-preservation` | `true` | `EUREKA_SELF_PRESERVATION` | Self-preservation mode |

## Profiles

| Profile | Use Case |
|---|---|
| *(default)* | Local development |
| `dev` | Dev with verbose Eureka logging |
| `docker` | Running inside Docker/Compose |

Activate: `--spring.profiles.active=dev`

## Client Configuration

All NearKart services that register with this server should include in their `application.yml`:

```yaml
spring:
  application:
    name: your-service-name   # MUST be unique

eureka:
  client:
    service-url:
      defaultZone: http://admin:admin123@localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## Endpoints

| Endpoint | Access | Description |
|---|---|---|
| `GET /` | Auth required | Eureka Dashboard UI |
| `GET /eureka/apps` | Auth required | All registered apps (XML/JSON) |
| `GET /actuator/health` | Public | Health status |
| `GET /actuator/info` | Auth required | App info |
| `GET /actuator/metrics` | Auth required | Metrics |

## Running Tests

```bash
./mvnw test
```
