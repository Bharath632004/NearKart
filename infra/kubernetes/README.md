# NearKart Kubernetes Manifests

All files are numbered in apply order.

## Files
| File | Creates |
|---|---|
| `00-namespace.yaml` | `nearkart` namespace |
| `01-secrets.yaml` | DB, Redis, App secrets |
| `02-configmap.yaml` | Shared non-sensitive env vars |
| `03-discovery-server.yaml` | Eureka + Service |
| `04-api-gateway.yaml` | API Gateway + Service + HPA |
| `05-backend-services.yaml` | All 11 backend microservices + Services |
| `06-frontend.yaml` | frontend + admin-panel + HPA |
| `07-ingress.yaml` | NGINX Ingress routing rules |

## Before Applying
1. Replace `<AWS_ACCOUNT_ID>` in all YAML files with your AWS account ID
2. Update `01-secrets.yaml` with real base64-encoded values from Terraform outputs:
```bash
# Get values from Terraform
cd infra/aws/terraform
terraform output rds_endpoint      # → paste into secrets url
terraform output redis_endpoint    # → paste into secrets host
```

## Deploy to EKS
```bash
# Connect kubectl to EKS
aws eks update-kubeconfig --region ap-south-1 --name $(terraform output -raw eks_cluster_name)

# Install NGINX Ingress Controller (one-time)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/aws/deploy.yaml

# Apply all manifests in order
kubectl apply -f infra/kubernetes/

# Verify
kubectl get pods -n nearkart
kubectl get ingress -n nearkart
```

## Build & Push All Images to ECR
```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REGION=ap-south-1
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com

for SERVICE in api-gateway auth-service user-service shop-service product-service inventory-service order-service payment-service delivery-service merchant-service notification-service analytics-service admin-service; do
  docker build -t nearkart/$SERVICE -f infra/docker/Dockerfile.backend-service backend/$SERVICE/
  docker tag nearkart/$SERVICE $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/nearkart/$SERVICE:latest
  docker push $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/nearkart/$SERVICE:latest
done
```
