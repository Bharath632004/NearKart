# Auth Service

Handles all authentication and authorization for NearKart.

## Features
- User Registration & Login
- OTP Verification (Firebase / SMS)
- JWT Token Generation & Refresh
- Password Hashing (BCrypt)
- Role-Based Access Control (RBAC)
- OAuth 2.0 Support

## Tech
- Java 21, Spring Boot, Spring Security
- PostgreSQL (user credentials)
- Redis (session/token cache)

## Endpoints
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/otp/verify`
- `POST /auth/refresh-token`
- `POST /auth/logout`
