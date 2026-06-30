# Inventory Service

The **Inventory Service** manages stock levels, inventory items, and stock transactions for the NearKart platform.

## Port
`8085`

## Tech Stack
- Java 17, Spring Boot 3.2
- Spring Data JPA + PostgreSQL
- Spring Security + JWT
- Eureka Service Discovery
- Lombok

## Features
- Create and manage inventory items (per product per shop)
- Real-time stock updates (STOCK_IN, STOCK_OUT, ADJUSTMENT, RESERVED, RELEASED, RETURNED)
- Low stock alerts
- Full stock transaction history/audit trail
- Stock availability check (used by Order Service)
- Auto status management (OUT_OF_STOCK, ACTIVE)

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/inventory` | MERCHANT/ADMIN | Create inventory item |
| GET | `/api/inventory/{id}` | Authenticated | Get item by ID |
| GET | `/api/inventory/product/{pid}/shop/{sid}` | Public | Get by product+shop |
| GET | `/api/inventory/shop/{shopId}` | Authenticated | All items in a shop |
| GET | `/api/inventory/product/{productId}` | Authenticated | All shops for a product |
| GET | `/api/inventory/low-stock` | MERCHANT/ADMIN | Low stock items |
| PATCH | `/api/inventory/{id}/stock` | MERCHANT/ADMIN/ORDER | Update stock |
| POST | `/api/inventory/check` | Public | Check availability |
| PUT | `/api/inventory/{id}` | MERCHANT/ADMIN | Update item details |
| PATCH | `/api/inventory/{id}/status` | MERCHANT/ADMIN | Update status |
| DELETE | `/api/inventory/{id}` | MERCHANT/ADMIN | Delete item |
| GET | `/api/inventory/{id}/transactions` | MERCHANT/ADMIN | Transaction history |

## Database Setup
```sql
CREATE DATABASE nearkart_inventory;
```

## Environment Variables
```
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=your_base64_encoded_secret
```

## Run
```bash
mvn spring-boot:run
```
