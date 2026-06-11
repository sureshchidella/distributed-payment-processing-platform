# High-Level Design (HLD) — Distributed Payment Simulator

> An event-driven microservices platform that simulates real-world payment processing pipelines using patterns from Mastercard and Axis Bank production systems.

---

## 1. System Overview

The **Distributed Payment Simulator** models the full lifecycle of a payment transaction — from initiation through fraud screening, account debit, ledger posting, and notification — using an asynchronous, event-driven architecture.

### Design Goals

| Goal | Description |
|------|-------------|
| **Loose Coupling** | Services communicate only through Kafka events — no direct inter-service REST calls |
| **Resilience** | Every failure path has a compensation strategy; no transaction is left in a limbo state |
| **Auditability** | Event sourcing provides a complete, immutable audit trail for every payment |
| **Scalability** | CQRS decouples read and write workloads; each can be scaled independently |
| **Exactly-Once Semantics** | Outbox pattern + idempotent consumers ensure no duplicate processing |

---

## 2. Architecture Diagram

```text
                           ┌─────────────┐
                           │   Client    │
                           └──────┬──────┘
                                  │ POST /payments/initiate
                                  ▼
                       ┌─────────────────────┐
                       │    API Gateway       │
                       │  Validation · Auth   │
                       │  Idempotency Cache   │
                       │        :8080         │
                       └─────────┬───────────┘
                                 │ CreatePaymentCommand (Axon)
                                 ▼
                  ┌────────────────────────────┐
                  │   Payment Service          │
                  │   Axon Aggregate · CQRS    │
                  │   Event Sourcing · Outbox  │
                  │          :8081             │
                  └────────────┬───────────────┘
                               │ PaymentInitiatedEvent → Outbox → Kafka
                               ▼

═════════════════════════════════════════════════════════════
                    APACHE KAFKA (:9092)
  Topics: payment.events · account.events · risk.events
═════════════════════════════════════════════════════════════

        │                │                │                │
        ▼                ▼                ▼                ▼

┌──────────────┐ ┌──────────────┐ ┌────────────────┐ ┌───────────────┐
│ Risk Service │ │Account Svc   │ │Notification Svc│ │ Query Service │
│ Fraud Rules  │ │Debit/Credit  │ │Webhook · DLT   │ │ Projections   │
│    :8082     │ │    :8083     │ │     :8085      │ │    :8084      │
└──────┬───────┘ └──────┬───────┘ └────────────────┘ └───────────────┘
       │                │           PostgreSQL :5434
       │                │ PostgreSQL :5433
       └──────┬─────────┘
              ▼

  ┌──────────────────────────────────────────────────┐
  │         Axon Saga Orchestrator (:8086)            │
  │  Coordinates multi-step distributed transactions  │
  │  Issues compensating commands on failure           │
  └──────────────────────────────────────────────────┘
```

---

## 3. Key Components

### 3.1 API Gateway (`:8080`)

- **Role:** Single external entry point for all client requests
- **Responsibilities:**
  - REST API endpoint (`POST /payments/initiate`)
  - Input validation (bean validation, currency whitelisting)
  - Idempotency enforcement via tiered cache (Caffeine → Redis → DB)
  - Forwards `CreatePaymentCommand` to Payment Service via Axon's `CommandGateway`
  - Returns `202 Accepted` with payment ID for async status polling
- **No business logic lives here**

### 3.2 Payment Service (`:8081`)

- **Role:** Core write model — the heart of the system
- **Responsibilities:**
  - Manages `PaymentAggregate` via Axon Framework (CQRS + Event Sourcing)
  - Handles commands: `CreatePaymentCommand`, `ConfirmPaymentCommand`, `CancelPaymentCommand`
  - Emits domain events: `PaymentInitiatedEvent`, `PaymentConfirmedEvent`, `PaymentCancelledEvent`
  - Implements the **Outbox Pattern** for reliable event publishing to Kafka
- **Database:** `payment_db` on PostgreSQL `:5432`
- **Kafka:** Produces to `payment.events`

### 3.3 Risk Service (`:8082`)

- **Role:** Fraud detection and screening
- **Responsibilities:**
  - Consumes `PaymentInitiatedEvent` from `payment.events`
  - Runs configurable fraud rules (velocity, amount threshold, blocklist, currency mismatch)
  - Publishes `RiskApprovedEvent` or `RiskRejectedEvent` to `risk.events`
- **Stateless** — no database required
- **Consumer group:** `risk-service`

### 3.4 Account Service (`:8083`)

- **Role:** Balance management with debit/credit operations
- **Responsibilities:**
  - Consumes `PaymentInitiatedEvent` from `payment.events`
  - Performs balance check and account debit with optimistic locking (`@Version`)
  - Tracks processed events for idempotency (prevents double-debit on Kafka redelivery)
  - Publishes `AccountDebitedEvent` or `AccountDebitFailedEvent` to `account.events`
- **Database:** `account_db` on PostgreSQL `:5433`
- **Consumer group:** `account-service`

### 3.5 Payment Saga Service (`:8086`)

- **Role:** Distributed transaction coordinator
- **Responsibilities:**
  - Axon Saga listens for events from Risk and Account services
  - If both succeed → sends `ConfirmPaymentCommand`
  - If either fails → sends `CancelPaymentCommand` (compensation)
  - Manages saga lifecycle with `@StartSaga` / `@EndSaga`
- **No database** — saga state managed by Axon Server

### 3.6 Query Service (`:8084`)

- **Role:** CQRS read side — denormalized projections
- **Responsibilities:**
  - Consumes ALL events from all Kafka topics
  - Maintains projection tables: `payment_summary`, `account_balance_view`, `payment_audit_log`
  - Exposes REST endpoints for status polling (`GET /payments/{id}`, `GET /accounts/{id}/balance`)
  - Completely decoupled from write model — can be rebuilt from event log at any time
- **Database:** `query_db` on PostgreSQL `:5434`
- **Consumer group:** `query-service`

### 3.7 Notification Service (`:8085`)

- **Role:** Downstream event notification and alerting
- **Responsibilities:**
  - Subscribes to all terminal events (`PaymentConfirmedEvent`, `PaymentCancelledEvent`, etc.)
  - Fires webhook stubs (structured logging)
  - Implements retry with exponential backoff
  - Dead Letter Topic (DLT) routing for permanently failing messages
- **Stateless** — no database required
- **Consumer group:** `notification-service`

---

## 4. Technology Stack

| Category | Technology | Rationale |
|----------|-----------|-----------|
| Language | Java 17 | Production standard in fintech |
| Framework | Spring Boot 3 | Ecosystem for Kafka, JPA, Actuator, Axon integration |
| CQRS / Event Sourcing | Axon Framework | Built-in aggregate management, saga orchestration, event store |
| Messaging | Apache Kafka | Backbone for async inter-service communication |
| Database | PostgreSQL | Each service owns its schema — no shared databases |
| Containerization | Docker Compose | Local dev stack: Kafka, Zookeeper, Axon Server, 3 PostgreSQL instances |
| Build Tool | Maven | Multi-module project |
| Testing | JUnit 5, Mockito, Testcontainers | Integration tests with real Kafka + PostgreSQL |

---

## 5. Communication Patterns

### 5.1 Synchronous (Client → Gateway only)

```
Client ──REST──→ API Gateway ──Axon CommandGateway──→ Payment Service
```

The only synchronous call in the system is from the client to the API Gateway, and from the Gateway to Payment Service via Axon's command bus.

### 5.2 Asynchronous (Everything else)

```
Payment Service ──Outbox → Kafka──→ Risk Service
Payment Service ──Outbox → Kafka──→ Account Service
Payment Service ──Outbox → Kafka──→ Query Service
Payment Service ──Outbox → Kafka──→ Notification Service
Risk Service    ──Kafka──→ Saga Service
Account Service ──Kafka──→ Saga Service
Saga Service    ──Axon──→ Payment Service (compensating commands)
```

All downstream communication flows through Kafka topics. Services are loosely coupled — they only know about the events they consume.

---

## 6. Data Architecture

### 6.1 Database-per-Service

| Service | Database | Port | Purpose |
|---------|----------|------|---------|
| Payment Service | `payment_db` | 5432 | Event store (Axon-managed) + outbox table |
| Account Service | `account_db` | 5433 | Accounts table + processed events (idempotency) |
| Query Service | `query_db` | 5434 | Denormalized projection tables |

### 6.2 Kafka Topics

| Topic | Producers | Consumers |
|-------|-----------|-----------|
| `payment.events` | Payment Service (via Outbox) | Risk, Account, Query, Notification, Saga |
| `risk.events` | Risk Service | Saga, Query, Notification |
| `account.events` | Account Service | Saga, Query, Notification |
| `notification.events` | Notification Service | (Dead Letter Topics) |

---

## 7. Transaction Flows

### 7.1 Happy Path — Successful Payment

```
Client → Gateway → Payment Service (CreatePaymentCommand)
  → PaymentInitiatedEvent → Kafka
    → Risk Service → RiskApprovedEvent → Kafka
    → Account Service → AccountDebitedEvent → Kafka
      → Saga (both events received) → ConfirmPaymentCommand
        → PaymentConfirmedEvent → Query Service updates projection
        → Notification Service fires success webhook
        → Client polls GET /payments/{id} → CONFIRMED
```

### 7.2 Failure Path — Insufficient Funds

```
Client → Gateway → Payment Service (CreatePaymentCommand)
  → PaymentInitiatedEvent → Kafka
    → Risk Service → RiskApprovedEvent → Kafka
    → Account Service → balance check fails
      → AccountDebitFailedEvent → Kafka
        → Saga → CancelPaymentCommand (compensation)
          → PaymentCancelledEvent → Query projection updated to CANCELLED
          → Notification Service fires failure alert
          → Client polls GET /payments/{id} → CANCELLED
```

### 7.3 Failure Path — Risk Rejection

```
Client → Gateway → Payment Service (CreatePaymentCommand)
  → PaymentInitiatedEvent → Kafka
    → Risk Service → fraud rule fails
      → RiskRejectedEvent → Kafka
        → Saga → CancelPaymentCommand (compensation)
          → PaymentCancelledEvent
          → Client polls → CANCELLED
```

---

## 8. Cross-Cutting Concerns

### 8.1 Idempotency

- **Gateway level:** Tiered cache (Caffeine → Redis → DB) keyed by `paymentReference`
- **Consumer level:** `processed_events` table in Account Service prevents double-debit on Kafka redelivery

### 8.2 Reliability

- **Outbox Pattern:** Eliminates dual-write failures between DB and Kafka
- **Dead Letter Topics:** Messages failing after max retries are routed to `{topic}.DLT`
- **Optimistic Locking:** `@Version` field on Account entity prevents concurrent balance corruption

### 8.3 Observability

- Correlation ID propagation (`X-Correlation-Id` header)
- Spring Boot Actuator health checks on all services
- Structured logging for all event processing

### 8.4 Security (Future)

- API authentication via JWT tokens
- mTLS between services
- Encrypted database credentials via Vault

---

## 9. Infrastructure

### Docker Compose Stack

| Container | Image | Port | Purpose |
|-----------|-------|------|---------|
| `zookeeper` | `confluentinc/cp-zookeeper:7.5.0` | 2181 | Kafka cluster coordination |
| `kafka` | `confluentinc/cp-kafka:7.5.0` | 9092 | Event bus |
| `axon-server` | `axoniq/axonserver:2024.1.2` | 8024 / 8124 | Event store + command routing |
| `postgres-payment` | `postgres:16-alpine` | 5432 | Payment database |
| `postgres-account` | `postgres:16-alpine` | 5433 | Account database |
| `postgres-query` | `postgres:16-alpine` | 5434 | Query database |

---

## 10. Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| Consistency | Eventual consistency via event-driven architecture |
| Availability | Each service can be independently deployed and restarted |
| Scalability | Services scale horizontally; Kafka consumer groups enable parallel processing |
| Fault Tolerance | Saga compensation, DLT, idempotent consumers, outbox relay |
| Data Integrity | ACID transactions within each service boundary; Outbox for cross-boundary |
| Latency | Sub-second for API Gateway response; eventual for downstream processing |

---

*Document Version: 1.0 · June 2026 · Distributed Payment Simulator*
