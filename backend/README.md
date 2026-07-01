# NearKart Backend — Microservices Architecture

A production-ready Spring Boot microservices backend for **NearKart**, a hyperlocal delivery platform connecting customers to nearby shops.

## Architecture Overview

```
                        ┌──────────────────┐
                        │   API Gateway    │  :8080
                        │  (JWT Filter +   │
                        │   Load Balance)  │
                        └────────┬─────────┘
                                 │
              ┌──────────────────▼──────────────────────┐
              │          Discovery Server (Eureka)       │  :8761
              └──────────────────┬──────────────────────┘
                                 │
    ┌────────────────────────────┼─────────────────────────────┐
    │                            │                             │
┌───▼────┐  ┌────────┐  ┌───────▼───┐  ┌────────┐  ┌────────┐│
│  Auth  │  │  User  │  │  Product  │  │  Shop  │  │ Order  ││
│ :8081  │  │ :8082  │  │   :8083   │  │ :8084  │  │ :8085  ││
└────────┘  └────────┘  └───────────┘  └────────┘  └────────┘│
┌────────┐  ┌─────────┐ ┌──────────┐  ┌──────────┐ ┌───────┐│
│Payment │  │Delivery │ │Notif.    │  │Inventory │ │Merch. ││
│ :8086  │  │  :8087  │ │  :8088   │  │  :8089   │ │ :8090 ││
└────────┘  └─────────┘ └──────────┘  └──────────┘ └───────┘│
┌──────────────┐  ┌────────────────┐                         │
│  Analytics   │  │     Admin      │                         │
│    :8091     │  │     :8092      │                         │
└──────────────┘  └────────────────┘                         │
    └────────────────────────────────────────────────────────┘
                     Kafka (Event Bus) + MySQL + Redis
```

## Services

| Service | Port | Responsibility |
|---|---|---|
| `discovery-server` | 8761 | Eureka service registry |
| `api-gateway` | 8080 | JWT auth, routing, rate-limit |
| `auth-service` | 8081 | Login, register, OTP, JWT |
| `user-service` | 8082 | User profiles, addresses |
| `product-service` | 8083 | Product CRUD, search, categories |
| `shop-service` | 8084 | Shop management, geo-search |
| `order-service` | 8085 | Cart, orders, order lifecycle |
| `payment-service` | 8086 | Payments, refunds |
| `delivery-service` | 8087 | Agent assignment, live tracking |
| `notification-service` | 8088 | Email, SMS, FCM push |
| `inventory-service` | 8089 | Stock management, low-stock alerts |
| `merchant-service` | 8090 | Merchant onboarding, shop approval |
| `analytics-service` | 8091 | Sales, orders, delivery analytics |
| `admin-service` | 8092 | Admin dashboard, platform management |

## Tech Stack

- **Framework**: Spring Boot 3.x + Spring Cloud
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Messaging**: Apache Kafka
- **Database**: MySQL 8 (separate DB per service)
- **Cache**: Redis
- **Auth**: JWT (JJWT)
- **Build**: Maven
- **Container**: Docker + Docker Compose

## Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### Run with Docker Compose

```bash
# 1. Clone the repo
git clone https://github.com/Bharath632004/NearKart.git
cd NearKart/backend

# 2. Copy and configure environment variables
cp .env.example .env
# Edit .env with your actual values

# 3. Build all services
mvn clean package -DskipTests

# 4. Start everything
docker-compose up -d

# 5. Check services are up
docker-compose ps
```

### Run Locally (Development)

```bash
# Start infrastructure only
docker-compose up -d mysql kafka zookeeper redis

# Start discovery server first
cd discovery-server && mvn spring-boot:run &

# Then start any service
cd auth-service && mvn spring-boot:run
```

## API Endpoints (via Gateway :8080)

### Auth
```
POST /api/auth/register      — Register user
POST /api/auth/login         — Login, returns JWT
POST /api/auth/refresh       — Refresh access token
POST /api/auth/logout        — Logout
POST /api/auth/otp/send      — Send OTP
POST /api/auth/otp/verify    — Verify OTP
```

### Orders
```
POST   /api/orders              — Place order
GET    /api/orders/{id}         — Get order details
GET    /api/orders/user/{uid}   — User order history
PUT    /api/orders/{id}/cancel  — Cancel order
```

### Cart
```
GET    /api/cart                — Get cart
POST   /api/cart/items          — Add item
DELETE /api/cart/items/{id}     — Remove item
DELETE /api/cart                — Clear cart
```

### Products
```
GET  /api/products              — List/search products
GET  /api/products/{id}         — Product detail
POST /api/products              — Create (merchant)
PUT  /api/products/{id}         — Update (merchant)
```

### Delivery
```
GET  /api/delivery/{orderId}    — Track delivery
PUT  /api/delivery/{id}/status  — Update status (agent)
```

## Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `order.placed` | order-service | payment, notification, inventory |
| `payment.confirmed` | payment-service | order, notification, delivery |
| `delivery.assigned` | delivery-service | notification |
| `delivery.status.updated` | delivery-service | order, notification, analytics |
| `inventory.low-stock` | inventory-service | notification, merchant |

## Environment Variables

See `.env.example` for all required variables.

## Inter-Service Communication

Services communicate via:
1. **Synchronous**: Feign Clients (REST) for real-time queries
2. **Asynchronous**: Kafka events for side-effects (notifications, analytics updates)

## Package Convention

All services use `in.nearkart.{service-name}` as the base package.

> **Note**: `analytics-service` and `api-gateway` currently use `com.nearkart.*` — to be migrated to `in.nearkart.*` in a future refactor.
