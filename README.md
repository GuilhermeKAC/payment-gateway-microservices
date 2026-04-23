# Payment Gateway with Microservices

A payment gateway built with Spring Boot and PostgreSQL using a microservices architecture. The system is composed of independent services that communicate via REST and asynchronous messaging.

## Microservices

| Service | Port | Description |
|---|---|---|
| API Gateway | 8080 | Entry point — routes and filters all incoming requests |
| Payment Service | 8081 | Handles payment processing and transactions |
| Merchant Service | 8082 | Manages merchants and customers |
| Webhook Service | 8083 | Delivers event notifications to external endpoints |

## Requirements

- Java 21
- PostgreSQL
- Redis
- RabbitMQ

## Running Locally

Start each service from its own directory:

```bash
# API Gateway
./mvnw spring-boot:run

# Payment Service
./mvnw spring-boot:run

# Merchant Service
./mvnw spring-boot:run

# Webhook Service
./mvnw spring-boot:run
```

## Docker

Docker instructions coming soon.
