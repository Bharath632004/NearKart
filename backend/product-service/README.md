# NearKart — Product Service

A Spring Boot microservice responsible for managing **Products** and **Categories** in the NearKart hyperlocal commerce platform.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT |
| Database | PostgreSQL (prod), H2 (test) |
| ORM | Spring Data JPA / Hibernate |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| Containerization | Docker |

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or Docker)
- Eureka Server running on port 8761

### Steps

```bash
# 1. Create the database
psql -U postgres -c "CREATE USER nearkart WITH PASSWORD 'nearkart123';"
psql -U postgres -c "CREATE DATABASE nearkart_products OWNER nearkart;"

# 2. Run the service
cd backend/product-service
mvn spring-boot:run
```

Service starts on **http://localhost:8082**

### Docker

```bash
docker build -t nearkart-product-service .
docker run -p 8082:8082 nearkart-product-service
```

## API Reference

Swagger UI → **http://localhost:8082/swagger-ui.html**

### Product Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/products` | Public | Get all products |
| GET | `/api/products/available` | Public | Get available products |
| GET | `/api/products/{id}` | Public | Get product by ID |
| GET | `/api/products/shop/{shopId}` | Public | Get products by shop |
| GET | `/api/products/category/{categoryId}` | Public | Get products by category |
| GET | `/api/products/search?keyword=` | Public | Search products |
| GET | `/api/products/filter/price?min=&max=` | Public | Filter by price range |
| GET | `/api/products/shop/{shopId}/count` | Public | Product count for shop |
| GET | `/api/products/out-of-stock` | ADMIN | Out-of-stock products |
| POST | `/api/products` | SELLER/ADMIN | Create product |
| PUT | `/api/products/{id}` | SELLER/ADMIN | Update product |
| PATCH | `/api/products/{id}/stock` | SELLER/ADMIN | Update stock quantity |
| PATCH | `/api/products/{id}/toggle` | SELLER/ADMIN | Toggle availability |
| DELETE | `/api/products/{id}` | SELLER/ADMIN | Delete product |

### Category Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/products/categories` | Public | Get all categories |
| GET | `/api/products/categories/{id}` | Public | Get category by ID |
| POST | `/api/products/categories` | ADMIN | Create category |
| PUT | `/api/products/categories/{id}` | ADMIN | Update category |
| DELETE | `/api/products/categories/{id}` | ADMIN | Delete category |

## Authentication

Protected endpoints require a Bearer JWT token:
```
Authorization: Bearer <token>
```

Tokens are issued by the **auth-service** and validated here via `JwtAuthFilter`.

## Roles

| Role | Permissions |
|------|-------------|
| `ROLE_ADMIN` | Full access — products + categories |
| `ROLE_SELLER` | Create / update / delete own products |
| Public | Read-only access to products and categories |

## Running Tests

```bash
mvn test
```

Tests use an in-memory **H2** database via `application-test.yml`.
