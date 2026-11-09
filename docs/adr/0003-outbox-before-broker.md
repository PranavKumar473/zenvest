# ADR 0003 — Transactional Outbox Before Introducing a Message Broker

**Date:** 2026-11-09
**Status:** Accepted

---

## Context

Zenvest needs reliable delivery of transactional emails (email verification, password reset, account deletion). Two architectural options exist:

1. **Message Broker (Kafka / RabbitMQ / SQS)** — business event published to a broker; email service consumes it.
2. **Transactional Outbox Pattern** — event written to an `outbox_events` table in the same database transaction as the business change; a poller reads and delivers it.

---

## Decision

We will implement the **Transactional Outbox Pattern** backed by PostgreSQL, using **Spring Modulith's Event Publication Registry** to manage the outbox table.

A ShedLock-coordinated poller runs every 5 seconds: `SELECT ... FOR UPDATE SKIP LOCKED LIMIT 50`. On delivery success, `sent_at` is marked. On failure, exponential backoff is applied up to 8 attempts, after which a metric and ERROR log alert the operator.

We will **not** introduce Kafka, RabbitMQ, or any external broker in v1.

---

## Consequences

**Positive:**
- **Zero extra infrastructure:** no broker to operate, monitor, or pay for on the Oracle Free Tier VM.
- **Exactly-once semantics** for writes: the outbox row is written atomically with the business transaction — no event is lost if the app crashes between the write and the send.
- **Two pollers never send the same event twice** because `SELECT ... FOR UPDATE SKIP LOCKED` is used (PostgreSQL advisory lock at the row level).
- Simple to test: Testcontainers provides a real PostgreSQL; no broker container needed.

**Negative:**
- Throughput is limited to what the PostgreSQL poller can handle — acceptable at v1 volumes (< 1,000 emails/day).
- If Zenvest grows to high-volume event streaming, we will add a broker and migrate. The outbox table schema is designed to be forward-compatible with debezium CDC, making future migration straightforward.

**Migration path:** when throughput demands it, replace the poller with a Debezium connector that reads the outbox table and publishes to Kafka — zero changes to the business modules.
