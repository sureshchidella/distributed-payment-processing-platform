# Distributed Payment Processing Platform

> ⚠️ Educational project inspired by real-world payment systems. It is not intended for production financial transactions.

A production-inspired event-driven payment processing platform built using Java, Spring Boot, Apache Kafka, PostgreSQL, CQRS, Event Sourcing, Saga Pattern, and Outbox Pattern.

This project simulates how modern financial systems process payments while maintaining consistency, fault tolerance, reliability, and scalability across distributed services.

---

## Overview

The platform models the complete lifecycle of a payment transaction:

1. Payment Initiation
2. Fraud Screening
3. Account Debit Processing
4. Transaction Confirmation
5. Read Model Projection
6. Notification Delivery

The system is fully asynchronous and event-driven using Apache Kafka.

---

## Architecture

```text
                           ┌─────────────┐
                           │   Client    │
                           └──────┬──────┘
                                  │
                                  ▼
                       ┌─────────────────────┐
                       │    API Gateway      │
                       └─────────┬───────────┘
                                 │
                                 ▼
                  ┌────────────────────────────┐
                  │ Payment Command Service    │
                  │ CQRS + Event Sourcing      │
                  │ Outbox Pattern             │
                  └────────────┬───────────────┘
                               │
                               ▼

══════════════════════════════════════════════════════
                    APACHE KAFKA
══════════════════════════════════════════════════════

        │                 │                 │
        ▼                 ▼                 ▼

┌──────────────┐ ┌──────────────┐ ┌─────────────────┐
│ Risk Service │ │Account Svc   │ │Notification Svc │
└──────┬───────┘ └──────┬───────┘ └─────────────────┘
       │                │
       └──────┬─────────┘
              ▼

      ┌───────────────────┐
      │ Payment Saga Svc  │
      └─────────┬─────────┘
                │
                ▼

      ┌───────────────────┐
      │ Query Service     │
      │ Read Model        │
      └───────────────────┘
```

---

# Tech Stack

| Category | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Messaging | Apache Kafka |
| Database | PostgreSQL |
| CQRS/Event Sourcing | Axon Framework |
| Containerization | Docker |
| Build Tool | Maven |
| API Documentation | OpenAPI / Swagger |
| Testing | JUnit 5, Mockito, Testcontainers |

---

# Core Concepts Demonstrated

## CQRS

Commands and Queries are completely separated.

### Command Side

- Create Payment
- Confirm Payment
- Cancel Payment

### Query Side

- Payment Status
- Transaction History
- Account Balance
- Audit Logs

---

## Event Sourcing

All state transitions are persisted as immutable domain events.

Example:

```text
PaymentInitiatedEvent
RiskApprovedEvent
AccountDebitedEvent
PaymentConfirmedEvent
```

Current state can be reconstructed by replaying events.

---

## Saga Pattern

Coordinates distributed transactions across multiple services.

### Success Flow

```text
Payment Initiated
       ↓
Risk Approved
       ↓
Account Debited
       ↓
Payment Confirmed
```

### Failure Flow

```text
Payment Initiated
       ↓
Risk Approved
       ↓
Insufficient Funds
       ↓
Compensation Triggered
       ↓
Payment Cancelled
```

---

## Outbox Pattern

Prevents dual-write failures.

Instead of:

```text
Write Database
Publish Kafka Event
```

The service performs:

```text
Write Database
Write Outbox Event
Commit Transaction
```

A dedicated Outbox Publisher later publishes events to Kafka.

---

## Idempotency

Prevents duplicate processing.

Implemented using:

- Idempotency Keys
- Kafka Producer Idempotence
- Processed Event Tracking
- Database Constraints

---

# Microservices

## API Gateway

### Responsibilities

- Authentication
- Request Validation
- Rate Limiting
- Idempotency Validation

### Endpoints

```http
POST /api/v1/payments
GET  /api/v1/payments/{id}
GET  /api/v1/accounts/{id}/balance
```

---

## Payment Command Service

Core write model of the platform.

### Responsibilities

- Command Handling
- Aggregate Management
- Event Storage
- Outbox Publishing

### Commands

```java
CreatePaymentCommand
ConfirmPaymentCommand
CancelPaymentCommand
```

### Events

```java
PaymentInitiatedEvent
PaymentConfirmedEvent
PaymentCancelledEvent
```

---

## Risk Service

Fraud detection and transaction validation.

### Checks

- Velocity Rules
- Transaction Limits
- Block Lists
- Currency Validation

### Events

```java
RiskApprovedEvent
RiskRejectedEvent
```

---

## Account Service

Account balance management.

### Responsibilities

- Debit Account
- Credit Account
- Balance Validation
- Idempotent Event Processing

### Events

```java
AccountDebitedEvent
AccountDebitFailedEvent
```

---

## Payment Saga Service

Orchestrates distributed transactions.

### Responsibilities

- Saga State Management
- Compensation Logic
- Event Coordination

### Example Compensation

```java
CancelPaymentCommand
```

Triggered when:

```java
AccountDebitFailedEvent
```

is received.

---

## Query Service

Maintains denormalized read models.

### Projection Tables

```text
payment_summary
account_balance_view
payment_audit_log
```

### Endpoints

```http
GET /api/v1/payments/{id}
GET /api/v1/accounts/{id}/balance
GET /api/v1/payments?status=FAILED
```

---

## Notification Service

Handles asynchronous notifications.

### Responsibilities

- Success Notifications
- Failure Notifications
- Webhook Delivery
- Retry Handling
- Dead Letter Processing

---

# Kafka Topics

```text
payment.initiated

risk.approved
risk.rejected

account.debited
account.debit.failed

payment.confirmed
payment.cancelled

notification.events
```

---

# Repository Structure

```text
distributed-payment-processing-platform

├── common-lib
│
├── infrastructure
│   ├── docker-compose
│   ├── kafka
│   └── postgres
│
├── api-gateway-service
├── payment-command-service
├── account-service
├── risk-service
├── payment-saga-service
├── payment-query-service
├── notification-service
│
└── docs
```

---

# Package Structure

```java
com.suresh.paymentsimulator
```

Example:

```text
com.suresh.paymentsimulator.payment.aggregate
com.suresh.paymentsimulator.payment.command
com.suresh.paymentsimulator.payment.event

com.suresh.paymentsimulator.account.consumer

com.suresh.paymentsimulator.risk.rules

com.suresh.paymentsimulator.query.projection
```

---

# Database Ownership

Each service owns its database.

```text
payment_db
account_db
risk_db
query_db
notification_db
saga_db
```

No service accesses another service's database directly.

---

# Example Transaction Flow

## Success Scenario

```text
PaymentInitiatedEvent
        ↓
RiskApprovedEvent
        ↓
AccountDebitedEvent
        ↓
PaymentConfirmedEvent
        ↓
NotificationSent
```

## Failure Scenario

```text
PaymentInitiatedEvent
        ↓
RiskApprovedEvent
        ↓
AccountDebitFailedEvent
        ↓
CancelPaymentCommand
        ↓
PaymentCancelledEvent
```

---

# Local Development Setup

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker
- Docker Compose

## Clone Repository

```bash
git clone https://github.com/<your-github-username>/distributed-payment-processing-platform.git

cd distributed-payment-processing-platform
```

## Start Infrastructure

```bash
docker compose up -d
```

This starts:

- Apache Kafka
- Zookeeper
- PostgreSQL
- Axon Server (Optional)

## Build

```bash
mvn clean install
```

## Run Services

```bash
mvn spring-boot:run
```

or

```bash
docker compose up
```

---

# Future Enhancements

## Ledger Service

Implement double-entry accounting.

```text
Debit Account A
Credit Account B
```

Every transaction produces balanced ledger entries.

## Additional Enhancements

- Multi-Currency Support
- Redis Caching
- Prometheus Metrics
- Grafana Dashboards
- Distributed Tracing
- Kubernetes Deployment
- Schema Registry
- Avro Serialization
- Event Replay Console

---

# What This Project Demonstrates

- Event-Driven Architecture
- Distributed Systems
- Microservices
- Apache Kafka
- CQRS
- Event Sourcing
- Saga Pattern
- Outbox Pattern
- Idempotent Processing
- Fault Tolerance
- Payment Processing Workflows
- Production-Style Backend Design

---

# Why This Project Exists

Most portfolio projects stop at CRUD operations.

This platform focuses on the problems backend engineers encounter in real financial systems:

- Distributed Transactions
- Event Consistency
- Reliable Messaging
- Failure Recovery
- Scalability
- Auditability

The goal is to demonstrate backend engineering skills expected in modern payment and banking systems.