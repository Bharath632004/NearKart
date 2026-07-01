# NearKart Docker

## Files
| File | Purpose |
|---|---|
| `Dockerfile.backend-service` | Generic multi-stage Dockerfile for all 13 Spring Boot microservices (Maven + Java 17) |
| `Dockerfile.frontend` | Multi-stage Next.js 14 (customer app, port 3000) |
| `Dockerfile.admin-panel` | Multi-stage Next.js 14 (admin panel, port 3001) |
| `docker-compose.yml` | Full local dev stack: Postgres, Redis, all microservices, NGINX |
| `.env.example` | Copy to `.env` — never commit `.env` |

## Quick Start (Local Dev)
```bash
cd infra/docker
cp .env.example .env
# Edit .env with your secrets
docker compose up --build
```

Services after startup:
- Frontend: http://localhost:3000
- Admin Panel: http://localhost:3001
- API Gateway: http://localhost:8080
- NGINX Proxy: http://localhost (port 80)
- Eureka Dashboard: http://localhost/eureka
- PostgreSQL: localhost:5432
- Redis: localhost:6379
