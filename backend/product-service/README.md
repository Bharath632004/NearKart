# NearKart - Product Service

Microservice responsible for managing products and categories in the NearKart platform.

## Port
`8082`

## Tech Stack
- Java 17
- Spring Boot 3.2
- Spring Security + JWT (stateless)
- Spring Data JPA + PostgreSQL
- Spring Cloud Netflix Eureka Client
- Lombok
- Maven

## API Endpoints

### Products
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/products` | Public | List all products |
| GET | `/api/products/{id}` | Public | Get product by ID |
| GET | `/api/products/shop/{shopId}` | Public | Products by shop |
| GET | `/api/products/category/{categoryId}` | Public | Products by category |
| GET | `/api/products/search?keyword=` | Public | Search products |
| POST | `/api/products` | SELLER/ADMIN | Create product |
| PUT | `/api/products/{id}` | SELLER/ADMIN | Update product |
| DELETE | `/api/products/{id}` | SELLER/ADMIN | Delete product |
| PATCH | `/api/products/{id}/toggle` | SELLER/ADMIN | Toggle availability |

### Categories
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/products/categories` | Public | List all categories |
| GET | `/api/products/categories/{id}` | Public | Get category by ID |
| POST | `/api/products/categories` | ADMIN | Create category |
| PUT | `/api/products/categories/{id}` | ADMIN | Update category |
| DELETE | `/api/products/categories/{id}` | ADMIN | Delete category |

## Running Locally

```bash
# Start PostgreSQL first, then:
mvn spring-boot:run
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/nearkart_products` | DB URL |
| `SPRING_DATASOURCE_USERNAME` | `nearkart` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `nearkart123` | DB password |
| `APP_JWT_SECRET` | (set in yml) | JWT signing key |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://localhost:8761/eureka/` | Eureka URL |

## Docker

```bash
docker build -t nearkart-product-service .
docker run -p 8082:8082 nearkart-product-service
```
