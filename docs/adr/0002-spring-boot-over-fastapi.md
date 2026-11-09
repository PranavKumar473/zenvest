# ADR 0002 — Spring Boot over FastAPI

**Date:** 2026-11-09
**Status:** Accepted

---

## Context

The developer has a Python background (FastAPI, pandas, numpy used in legacy reference code). The project requires a production-grade backend with security, database migrations, caching, rate limiting, observability, and an email outbox — all battle-tested and well-integrated.

We evaluated two options:
1. **FastAPI (Python)** — familiar language, rapid prototyping, excellent async support.
2. **Spring Boot (Java)** — industry-standard for Indian fintech backends, mature ecosystem, strong type safety.

---

## Decision

We will use **Spring Boot 4.1.x on Java 25**.

Key reasons:
- **Spring Security** provides production-grade JWT RS256, OAuth2 Resource Server, and password encoding out of the box; replicating this in FastAPI requires assembling many libraries.
- **Spring Data JPA + Flyway + Testcontainers** is a proven, integrated stack for relational data with safe migrations; the equivalent Python stack is fragmented.
- **Spring Modulith** enforces architectural boundaries at compile-time and test-time — no equivalent exists in the Python ecosystem.
- **Virtual Threads (Java 25 / Project Loom)** give near-async throughput with simple blocking code — bridging the FastAPI async advantage.
- Indian fintech employers overwhelmingly use Java/Spring; learning it directly accelerates the developer's career.
- The Python financial logic (`financial_math.py`, `risk_metrics_service.py`, `budget_calculator.py`) will be **ported** to Java — not called via subprocess — keeping the deployment simple and type-safe.

---

## Consequences

**Positive:**
- Rich, integrated ecosystem: Security, Cache, Scheduler, Actuator, OpenTelemetry all from one BOM.
- Strong typing catches entire classes of bugs at compile time.
- Career-relevant technology for the Indian job market.

**Negative:**
- Steeper initial learning curve versus Python for the developer.
- Slower cold-start than FastAPI — mitigated by keeping the app always running (single VM, Docker restart policy).
- Python data-science libraries (pandas, scipy) are unavailable — analytics implemented in pure Java with unit-tested formulas.
