# Kubernetes Configuration

Helm charts and K8s manifests for AWS EKS deployment.

## Structure
```
kubernetes/
  namespaces/
  deployments/
  services/
  ingress/
  configmaps/
  secrets/
  hpa/          # Horizontal Pod Autoscaler
```

## Deploy
```bash
kubectl apply -f namespaces/
helm install nearkart ./charts/nearkart
```
