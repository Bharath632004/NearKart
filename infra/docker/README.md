# Docker Configuration

Each microservice has its own Dockerfile. Use Docker Compose for local development.

## Local Dev
```bash
docker-compose up --build
```

## Services Started
- All 12 microservices
- PostgreSQL
- Redis
- Apache Kafka + Zookeeper
- Nginx
