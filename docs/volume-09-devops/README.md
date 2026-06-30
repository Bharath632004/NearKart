# Volume 9 – DevOps (80+ Pages)

## DevOps Pipeline

### Version Control
- Git + GitHub (this repository)
- Branch strategy: `main`, `develop`, `feature/*`, `hotfix/*`

### CI/CD
- GitHub Actions (`.github/workflows/`)
- On push to `develop`: run tests, build Docker images
- On merge to `main`: deploy to AWS EKS

### Containerization
- Docker (each microservice has its own Dockerfile)
- Docker Compose for local development

### Orchestration
- Kubernetes (AWS EKS)
- Helm charts for service deployment

### Reverse Proxy
- Nginx (ingress controller)

### Cloud Infrastructure
- AWS EC2, EKS, RDS (PostgreSQL), ElastiCache (Redis)
- AWS S3 (media storage), CloudFront (CDN)

### Monitoring
- Prometheus (metrics collection)
- Grafana (dashboards and alerting)

### Logging
- ELK Stack: Elasticsearch + Logstash + Kibana

### Backup & Recovery
- Automated daily RDS snapshots
- S3 versioning for media files
- Disaster recovery runbook
