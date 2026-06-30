# NearKart – Notification Service

Microservice responsible for sending **Email**, **SMS**, and **Push (FCM)** notifications, driven by **Kafka** events.

## Tech Stack
| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.3 + Java 21 |
| Email | Spring Mail + Thymeleaf templates |
| SMS | Twilio SDK |
| Push | Firebase Admin SDK (FCM) |
| Events | Apache Kafka |
| DB | PostgreSQL (notification_logs, device_tokens) |
| Container | Docker |

## Kafka Topics Consumed
| Topic | Triggered by |
|---|---|
| `order.placed` | Order Service |
| `order.shipped` | Order Service |
| `order.delivered` | Order Service |
| `order.cancelled` | Order Service |
| `payment.success` | Payment Service |
| `payment.failed` | Payment Service |
| `notification.otp` | Auth Service |

## REST API
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/notifications/email` | Send email directly |
| POST | `/api/v1/notifications/sms` | Send SMS directly |
| POST | `/api/v1/notifications/push` | Send push notification |
| POST | `/api/v1/notifications/device-token` | Register FCM token |
| DELETE | `/api/v1/notifications/device-token/{token}` | Deregister token |
| GET | `/api/v1/notifications/device-token/user/{userId}` | Get user tokens |
| GET | `/api/v1/notifications/logs/user/{userId}` | Get notification history |
| GET | `/api/v1/notifications/logs/{id}` | Get single log |
| GET | `/api/v1/notifications/health` | Health check |

## Running Locally
```bash
cp .env.example .env
# fill in .env values
mvn spring-boot:run
```

## Docker
```bash
docker build -t nearkart/notification-service .
docker run --env-file .env -p 8085:8085 nearkart/notification-service
```
