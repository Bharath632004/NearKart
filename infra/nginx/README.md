# NearKart NGINX Config

## Files
| File | Used By |
|---|---|
| `nginx.conf` | `docker-compose.yml` local dev NGINX container |

> For production (EKS), NGINX Ingress Controller is used — config lives in `infra/kubernetes/07-ingress.yaml`.

## Traffic Flow
```
Internet
  └── CloudFront (CDN + SSL termination)
        └── AWS Load Balancer (created by NGINX Ingress Controller)
              └── NGINX Ingress (Kubernetes)
                    ├── /api/*     → api-gateway:8080
                    ├── admin.*    → admin-panel:3001
                    └── /*         → frontend:3000
```
