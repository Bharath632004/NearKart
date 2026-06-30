# NearKart API Gateway

Spring Cloud Gateway acting as the single entry point for all NearKart microservices.

## Features

| Feature | Detail |
|---|---|
| **Routing** | Dynamic routes to user, product, order, shop services via Eureka |
| **JWT Auth** | Custom `JwtAuthGatewayFilterFactory` validates Bearer tokens |
| **Rate Limiting** | Redis-backed per-IP rate limiter (20 req/s, burst 40) |
| **Circuit Breaker** | Resilience4j CB with fallback at `/fallback` |
| **CORS** | Configurable allowed origins, methods, headers |
| **Logging** | Global request/response logging with correlation ID |
| **Security Headers** | HSTS, X-Frame-Options, X-XSS-Protection on every response |
| **Metrics** | Prometheus metrics via Actuator |

## Port

```
Gateway  : http://localhost:8080
Eureka   : http://localhost:8761
Redis    : localhost:6379
```

## Running Locally

```bash
# 1. Start Redis
docker run -d -p 6379:6379 redis:alpine

# 2. Start Eureka (from service-registry module)

# 3. Start gateway
mvn spring-boot:run
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | `NearKart...` | HS256 signing secret (min 32 chars) |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka service URL |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `FRONTEND_URL` | `http://localhost:3000` | Allowed CORS origin |

## Route Map

```
GET/POST /api/auth/**      → user-service   (public – no JWT needed)
Any      /api/users/**     → user-service   (JWT required)
Any      /api/products/**  → product-service (JWT required)
Any      /api/orders/**    → order-service  (JWT required)
Any      /api/shops/**     → shop-service   (JWT required)
```

## Docker Build

```bash
docker build -t nearkart/api-gateway .
docker run -e JWT_SECRET=YourSecretHere -e EUREKA_URL=http://eureka:8761/eureka/ -p 8080:8080 nearkart/api-gateway
```
