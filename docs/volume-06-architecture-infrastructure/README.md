# Volume 6 – Architecture & Infrastructure (300+ Pages)

## Microservices

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | Central routing, JWT validation, rate limiting |
| auth-service | 8081 | Auth, OTP, JWT, RBAC |
| user-service | 8082 | Customer profiles, addresses, wallet |
| merchant-service | 8083 | Shop management, KYC, settlements |
| product-service | 8084 | Product catalog, categories, search |
| inventory-service | 8085 | Stock management per shop |
| order-service | 8086 | Order lifecycle management |
| payment-service | 8087 | Razorpay, refunds, transactions |
| delivery-service | 8088 | Delivery partner ops, live tracking |
| notification-service | 8089 | FCM, SMS, Email notifications |
| analytics-service | 8090 | Reports and dashboard data |
| admin-service | 8091 | Platform administration |

## Technology Stack

### Backend
- Java 21, Spring Boot 3.x
- Spring Security, Spring Cloud, Spring Data JPA

### Frontend
- React.js, Next.js 14, Tailwind CSS

### Mobile
- Flutter 3.x (Android & iOS)

### Database
- PostgreSQL 16 + PostGIS
- Redis 7.x

### Messaging
- Apache Kafka 3.x

### Cloud
- AWS (EC2, S3, RDS, EKS, CloudFront)

### Maps & Location
- Google Maps Platform (Maps, Routes, Places API)

### Payments
- Razorpay (UPI, Cards, Wallets, Netbanking)

### Notifications
- Firebase Cloud Messaging (FCM)

### Deployment
- Docker, Kubernetes (EKS), Nginx

### Monitoring
- Prometheus + Grafana
- ELK Stack (Elasticsearch, Logstash, Kibana)
