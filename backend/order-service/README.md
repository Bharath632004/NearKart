# Order Service

Core order lifecycle management from placement to delivery.

## Features
- Order Placement
- Order Acceptance / Rejection by Merchant
- Order Status Tracking (real-time)
- Order Cancellation & Refund Trigger
- Coupons & Discount Application
- Invoice Generation

## Tech
- Java 21, Spring Boot
- PostgreSQL
- Apache Kafka (order events → delivery, notification)
- Redis (cart session)
