# Distributed Payment Processing Platform

> An event-driven microservices system that simulates real-world payment processing pipelines — built from first-hand experience working on Mastercard's payment transfer platform and Axis Bank's lending systems.

---

## Why This Project Exists

Most backend portfolio projects demonstrate CRUD operations with a REST API and a database. That's not what payment systems look like in production.

After spending 3+ years building payment transfers, loan processing, and API integrations at **Mastercard** and **Axis Bank** — supporting **500,000+ daily transactions** across multiple regions — I built this project to demonstrate the distributed systems patterns I've worked with at scale, in a form I can openly share.

At Mastercard, I worked on a **Unified Payment API** handling both domestic and cross-border transfers. At Axis Bank, I built loan management microservices that integrate with external verification workflows. In both environments, the hard problems weren't "how do you build a REST endpoint" — they were:

- **How do you process a payment across 4 services without losing data if one crashes mid-transaction?**
- **How do you prevent a customer from being charged twice when Kafka redelivers a message?**
- **How do you split read and write workloads so query traffic doesn't throttle payment processing?**
- **How do you roll back a partially completed payment when the account service reports insufficient funds?**

This project answers those questions with working code.

---

## What It Simulates

The platform models the **full lifecycle of a payment transaction** — from initiation through fraud screening, account debit, ledger posting, and notification — using patterns drawn from production fintech systems:

1. **Payment Initiation** — Client submits a payment request with an idempotency key
2. **Fraud Screening** — Risk service evaluates velocity rules, amount thresholds, and blocklists
3. **Account Debit** — Account service checks balance and debits the sender
4. **Transaction Confirmation** — Saga orchestrator confirms the payment after all steps succeed
5. **Read Model Projection** — Query service updates denormalized views for fast polling
6. **Notification Delivery** — Notification service fires webhooks and logs event outcomes

The entire flow is **asynchronous and event-driven**. After the API Gateway accepts a request, every downstream step communicates through Apache Kafka. No synchronous inter-service REST calls.

---

## Architecture

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
                       └─────────┬───────────┘
                                 │ CreatePaymentCommand
                                 ▼
                  ┌────────────────────────────┐
                  │   Payment Service          │
                  │   Axon Aggregate · CQRS    │
                  │   Event Sourcing · Outbox  │
                  └────────────┬───────────────┘
                               │ PaymentInitiatedEvent
                               ▼

═══════════════════════════════════════════════════════
                    APACHE KAFKA
  Topics: payment.events · account.events · risk.events
═══════════════════════════════════════════════════════

        │                │                │                │
        ▼                ▼                ▼                ▼

┌──────────────┐ ┌──────────────┐ ┌────────────────┐ ┌───────────────┐
│ Risk Service │ │Account Svc   │ │Notification Svc│ │ Query Service │
│ Fraud Rules  │ │Debit/Credit  │ │Webhook · DLT   │ │ Projections   │
└──────┬───────┘ └──────┬───────┘ └────────────────┘ └───────────────┘
       │                │
       └──────┬─────────┘
              ▼

  ┌──────────────────────────────────────────────────┐
  │         Axon Saga Orchestrator                   │
  │  Coordinates multi-step distributed transactions │
  │  Issues compensating commands on failure         │
  └──────────────────────────────────────────────────┘
```

**Key architectural decisions:**
- Each service **owns its own database** — no shared schemas, no cross-service queries
- All inter-service communication flows through **Kafka topics** — services are producers and consumers
- The **Outbox Pattern** eliminates dual-write failures between the database and Kafka
- The **Saga Orchestrator** manages distributed transactions without distributed locks

---

## Tech Stack

| Category | Technology | Why |
|----------|-----------|-----|
| Language | Java 17 | Production standard across Mastercard, Axis Bank, and most fintech backends |
| Framework | Spring Boot 3 | Ecosystem for Kafka, JPA, Actuator, and Axon integration |
| CQRS / Event Sourcing | Axon Framework | Separates command and query models with built-in Saga orchestration |
| Messaging | Apache Kafka | Backbone for all async inter-service communication — used in production at Mastercard |
| Database | PostgreSQL | Each service has its own schema — `payment_db`, `account_db`, `query_db` |
| Containerization | Docker Compose | Full local stack: Kafka, Zookeeper, Axon Server, 3 PostgreSQL instances |
| Build Tool | Maven | Multi-module project structure |
| Testing | JUnit 5, Mockito, Testcontainers | Integration tests with real Kafka + PostgreSQL containers |
| API Docs | OpenAPI / Swagger | Auto-generated from annotations |

---

## Core Patterns — And Why Each One Matters

These patterns aren't used for demonstration purposes alone. Each one solves a specific failure mode I've encountered in production payment systems.

### CQRS (Command Query Responsibility Segregation)

**The problem:** In a payment system, write throughput (processing transactions) and read throughput (status polling, dashboards, audits) have completely different scaling characteristics. If they share a model, read-heavy traffic degrades payment processing.

**The solution:** Commands (`CreatePaymentCommand`, `ConfirmPaymentCommand`, `CancelPaymentCommand`) are handled by Axon Aggregates that enforce business invariants and emit domain events. Queries read from **denormalized projection tables** — zero joins, sub-millisecond reads, completely decoupled from write throughput.

```java
// Write side — enforces invariants, emits events
@CommandHandler
void handle(CreatePaymentCommand cmd) {
    // validate, then:
    AggregateLifecycle.apply(new PaymentInitiatedEvent(...));
}

// Read side — separate service, separate DB
@QueryHandler
PaymentDTO handle(FindPaymentQuery query) {
    return projectionRepository.findById(query.getPaymentId());
}
```

### Event Sourcing

**The problem:** Traditional CRUD overwrites state. When a payment goes wrong, you can't reconstruct what happened — you only know the final state.

**The solution:** All state transitions are persisted as **immutable domain events**. The aggregate's current state is reconstructed by replaying events. Projections can be rebuilt from scratch at any time — no data is ever lost.

```
PaymentInitiatedEvent  →  RiskApprovedEvent  →  AccountDebitedEvent  →  PaymentConfirmedEvent
```

### Saga Pattern (Orchestration)

**The problem:** A payment touches multiple services (risk, account, notification). There's no distributed transaction manager. If the account debit fails after risk approval, you need to undo the payment — without a distributed lock.

**The solution:** The Axon Saga tracks the multi-step flow. On any failure, it issues **compensating commands** to undo committed steps:

```java
@SagaEventHandler
void on(AccountDebitFailedEvent event) {
    // Risk already approved, but funds insufficient — compensate
    commandGateway.send(new CancelPaymentCommand(
        event.getPaymentId(), "INSUFFICIENT_FUNDS"
    ));
}
```

### Outbox Pattern

**The problem:** After processing a payment, the service needs to (1) update its database and (2) publish an event to Kafka. If the service crashes between these two steps, data and events diverge. This is the **dual-write problem**.

**The solution:** The Payment Service writes its domain event **and** an outbox record in a **single ACID transaction**. A relay process polls the outbox table and publishes to Kafka — only marking rows as published after receiving Kafka's ACK.

```sql
-- Single atomic transaction
BEGIN;
  INSERT INTO payments (...) VALUES (...);
  INSERT INTO outbox_events (payload, published) VALUES (:event, false);
COMMIT;
-- Relay polls unpublished rows → publishes to Kafka → marks published
```

### Idempotent Consumers

**The problem:** Kafka guarantees at-least-once delivery. If a consumer crashes after processing a message but before committing its offset, the message will be redelivered. A payment could be debited twice.

**The solution:** Each service stores processed event IDs in PostgreSQL. Duplicate events are discarded before any business logic executes:

```java
if (processedEventRepo.existsById(event.getId())) {
    return; // already handled — skip silently
}
// process + insert ID atomically
```

### Dead Letter Topic (DLT)

**The problem:** Some messages will always fail — malformed payloads, unavailable dependencies, bugs. Without a safety net, they block the consumer partition forever.

**The solution:** Messages that fail after max retries are routed to a Dead Letter Topic for inspection, replay, and root-cause analysis — without losing events permanently.

---

## Microservices Breakdown

### 01 · API Gateway
**Entry Point · REST · Request Validation**

Single external entry point. Validates input, checks the idempotency key (returns cached response if duplicate), and forwards `CreatePaymentCommand` to the Payment Service via Axon's CommandGateway. Returns `202 Accepted` with a payment ID for async polling. No business logic lives here.

**Endpoint:** `POST /payments/initiate`

---

### 02 · Payment Service
**Core Write Model · CQRS · Event Sourcing · Outbox**

The heart of the system. Manages the `PaymentAggregate` via Axon. All state changes produce domain events persisted to Axon's event store. The Outbox relay handles publishing to Kafka without dual-write risk.

**Commands:** `CreatePaymentCommand` → `ConfirmPaymentCommand` → `CancelPaymentCommand`
**Events:** `PaymentInitiatedEvent` → `PaymentConfirmedEvent` / `PaymentCancelledEvent`

---

### 03 · Account Service
**Balance Management · Debit / Credit · Idempotent**

Consumes `PaymentInitiatedEvent` from Kafka and attempts to debit the sender account. Publishes the result as a new event. Uses optimistic locking and processed-event tracking to prevent double-debit on redelivery.

**Events:** `AccountDebitedEvent` / `AccountDebitFailedEvent`

---

### 04 · Risk Service
**Fraud Detection · Rule Engine · Async Screening**

Screens every payment against configurable fraud rules before account debit is allowed:
- Velocity check (> N transactions per minute from same sender)
- Amount threshold (single payment above configurable limit)
- Blocklist check (sender or receiver on block list)
- Currency mismatch between sender and receiver

**Events:** `RiskApprovedEvent` / `RiskRejectedEvent`

---

### 05 · Notification Service
**Downstream Alerts · Webhook · Event Log**

Subscribes to all final-state events (`PaymentConfirmedEvent`, `PaymentCancelledEvent`, `RiskRejectedEvent`, `AccountDebitFailedEvent`). Fully async and independently scalable — designed to be slow without blocking payment processing. Uses exponential backoff retry and Dead Letter Topic for permanently failing notifications.

---

### 06 · Query Service
**Read Model · Projections · REST Queries**

Maintains denormalized projection tables by consuming events from Kafka. Completely decoupled from the write model — can be rebuilt from the event log at any time.

**Projections:** `payment_summary` · `account_balance_view` · `payment_audit_log`

**Endpoints:**
```
GET /payments/{id}           → current status + event trail
GET /accounts/{id}/balance   → latest projected balance
GET /payments?status=FAILED  → filtered query with pagination
```

---

## Transaction Flows

### ✅ Happy Path — Successful Payment

| Step | Action | Output |
|------|--------|--------|
| 1 | Client sends `POST /payments/initiate` with idempotency key | REST |
| 2 | Gateway validates, issues `CreatePaymentCommand` to Axon | CMD |
| 3 | PaymentAggregate emits `PaymentInitiatedEvent` to event store + outbox | EVENT |
| 4 | Outbox relay publishes event to `payment.events` Kafka topic | KAFKA |
| 5 | Risk Service consumes, runs rules, emits `RiskApprovedEvent` | RISK ✓ |
| 6 | Account Service debits sender, emits `AccountDebitedEvent` | DEBIT ✓ |
| 7 | Saga receives both events, sends `ConfirmPaymentCommand` | SAGA |
| 8 | Aggregate emits `PaymentConfirmedEvent`; Query Service updates projection | DONE |
| 9 | Notification Service fires webhook. Client polls GET → `CONFIRMED` | NOTIF |

### ❌ Failure Path — Insufficient Funds

| Step | Action | Output |
|------|--------|--------|
| 1 | Steps 1–4 identical — payment initiated, published to Kafka | INIT |
| 2 | Risk Service approves → `RiskApprovedEvent` | RISK ✓ |
| 3 | Account Service checks balance — insufficient funds | CHECK |
| 4 | Account Service emits `AccountDebitFailedEvent` with reason | FAIL |
| 5 | Saga receives failure, triggers compensation | SAGA |
| 6 | Saga sends `CancelPaymentCommand` with reason `INSUFFICIENT_FUNDS` | COMP |
| 7 | Aggregate emits `PaymentCancelledEvent`; projection updated to `CANCELLED` | CANCEL |
| 8 | Notification Service fires failure alert. Client polls → `CANCELLED` | NOTIF |

> Both paths are fully implemented and testable. The failure path demonstrates the complete Saga compensation cycle — not an afterthought.

---

## Kafka Configuration

| Config Key | Value | Why |
|------------|-------|-----|
| `enable.idempotence` | `true` | Producer won't duplicate messages on retry. Required for exactly-once semantics |
| `transactional.id` | `payment-producer-1` | Enables Kafka transactions. Epoch fencing prevents zombie producers |
| `acks` | `all` | All ISR replicas must ACK. No data loss on leader failure |
| `isolation.level` | `read_committed` | Consumer skips uncommitted transactional messages |
| `auto.offset.reset` | `earliest` | New consumer groups start from beginning — safe for projection rebuild |
| `enable.auto.commit` | `false` | Manual commit after idempotency check + business logic |
| `max.in.flight.per.connection` | `5` | Pipeline parallelism while idempotent producer preserves ordering |
| Outbox poll interval | `200ms` | Balance between latency and DB load |

---

## Infrastructure (Docker Compose)

| Service | Port | Purpose |
|---------|------|---------|
| `kafka` | 9092 | Event bus for all inter-service communication |
| `zookeeper` | 2181 | Kafka cluster coordination |
| `axon-server` | 8024 / 8124 | Event store + command routing |
| `postgres-payment` | 5432 | Payment service database (`payment_db`) |
| `postgres-account` | 5433 | Account service database (`account_db`) |
| `postgres-query` | 5434 | Query service projections (`query_db`) |

---

## Repository Structure

```text
distributed-payment-processing-platform/
│
├── api-gateway/              # REST entry point, validation, idempotency
├── payment-service/          # CQRS write model, Axon aggregates, outbox
├── common-service/           # Shared DTOs, events, commands
│
├── docker-compose.yml        # Full local infrastructure
└── README.md
```

**Planned additions:**
```text
├── account-service/          # Balance management, debit/credit
├── risk-service/             # Fraud detection, rule engine
├── notification-service/     # Webhooks, DLT, retry logic
├── query-service/            # Read model, projections
├── payment-saga-service/     # Saga orchestrator, compensation
└── infrastructure/           # Kafka config, PostgreSQL schemas
```

---

## Package Structure

```
com.suresh.paymentsimulator
├── payment.aggregate         # Axon aggregates, command handlers
├── payment.command           # Command definitions
├── payment.event             # Domain event definitions
├── payment.outbox            # Outbox table + relay/poller
├── account.consumer          # Kafka consumer, debit logic
├── risk.rules                # Fraud rule engine
├── query.projection          # Event handlers, projection tables
└── notification.handler      # Webhook delivery, DLT routing
```

---

## Local Development

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### Quick Start

```bash
# Clone
git clone https://github.com/sureshchidella/distributed-payment-processing-platform.git
cd distributed-payment-processing-platform

# Start infrastructure (Kafka, PostgreSQL, Axon Server)
docker compose up -d

# Build
mvn clean install

# Run services
mvn spring-boot:run
```

---

## Future Enhancements

- **Ledger Service** — Double-entry accounting (debit Account A, credit Account B)
- **Multi-Currency Support** — Currency conversion with exchange rate service
- **Redis Caching** — Idempotency key cache, balance lookups
- **Prometheus + Grafana** — Payment latency histograms, request rate dashboards
- **Distributed Tracing** — Zipkin/Jaeger traces across the full payment flow
- **Kubernetes Deployment** — Helm charts for production-like deployment
- **Schema Registry** — Avro serialization with backward/forward compatibility
- **Event Replay Console** — Rebuild projections from event store on demand

---

## What This Project Demonstrates

This isn't a tutorial project. It's a working demonstration of the patterns backend engineers encounter in **real financial systems**:

| Concept | Implementation |
|---------|---------------|
| Distributed Transactions | Saga orchestration with compensating commands |
| Event Consistency | Event Sourcing via Axon — immutable, replayable |
| Reliable Messaging | Outbox pattern eliminates dual-write failures |
| Exactly-Once Processing | Idempotent producers + consumer event tracking |
| Failure Recovery | Full compensation cycle on any step failure |
| Read/Write Separation | CQRS with independent projection databases |
| Fault Tolerance | Dead Letter Topics, circuit breakers, retry policies |
| Auditability | Complete event trail per payment — every state transition recorded |

The goal: demonstrate backend engineering depth expected in modern payment and banking systems — not just that I can build an API, but that I understand what happens when things fail at scale.

---

<p align="center">
  <sub>Built by <a href="https://github.com/sureshchidella">Suresh Chidella</a> · Java 17 · Spring Boot 3 · Apache Kafka · Axon Framework · PostgreSQL</sub>
</p>