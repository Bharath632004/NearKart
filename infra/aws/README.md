# AWS Infrastructure

## Services Used

| AWS Service | Purpose |
|---|---|
| EC2 | Application servers |
| EKS | Kubernetes cluster |
| RDS (PostgreSQL) | Primary database |
| ElastiCache (Redis) | Caching layer |
| S3 | Media storage (images, KYC docs) |
| CloudFront | CDN for frontend & media |
| SES | Email notifications |
| SNS | SMS notifications |
| Route 53 | DNS management |
| ACM | SSL/TLS certificates |
| CloudWatch | Logging & monitoring |

## IaC
- Terraform scripts in `aws/terraform/`
