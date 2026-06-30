# 👤 User Service – NearKart

Manages user profiles and saved delivery addresses.

## Features
- Get & update user profile (name, bio, profile picture)
- Manage multiple delivery addresses (HOME, WORK, OTHER)
- Set default address
- JWT validation (shared secret with auth-service)
- Internal endpoint for profile creation after registration

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/me` | Get my profile |
| PUT | `/api/v1/users/me` | Update my profile |
| GET | `/api/v1/users/me/addresses` | List my addresses |
| POST | `/api/v1/users/me/addresses` | Add new address |
| DELETE | `/api/v1/users/me/addresses/{id}` | Delete address |
| POST | `/api/v1/users/internal/create` | Internal: Create profile |

## Running Locally

```bash
mvn spring-boot:run
# Swagger UI: http://localhost:8082/swagger-ui.html
```
