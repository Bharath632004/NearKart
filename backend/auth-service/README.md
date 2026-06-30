# 🔐 Auth Service – NearKart

Handles all authentication and token management for the NearKart platform.

## Features
- JWT Access Token (15 min) + Refresh Token (7 days) with rotation
- BCrypt password hashing (strength 12)
- Register / Login (email or phone) / Logout / Token Refresh
- Token validation endpoint for API Gateway
- Spring Security stateless configuration
- Eureka service registration

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login (email or phone) |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | Logout and revoke token |
| GET  | `/api/v1/auth/validate` | Validate token (for API Gateway) |

## Running Locally

```bash
# Requires PostgreSQL and Redis running
mvn spring-boot:run
# Swagger UI: http://localhost:8081/swagger-ui.html
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| DB_USERNAME | nearkart | PostgreSQL username |
| DB_PASSWORD | nearkart123 | PostgreSQL password |
| REDIS_HOST | localhost | Redis host |
| JWT_SECRET | (set in prod) | 256-bit JWT signing key |
