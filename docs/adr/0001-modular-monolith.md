# ADR 0001 — Modular Monolith Architecture

**Date:** 2026-11-09
**Status:** Accepted

---

## Context

Zenvest is a personal-finance tracker with an Indian mutual fund research module. The team is a single MCA student developer building v1. We need a system that is easy to build, debug, and deploy alone while remaining decomposable into microservices later if the product grows.

We evaluated three architectural styles:
1. **Microservices** — each module (identity, ledger, funds, etc.) is a separate deployable service.
2. **Modular Monolith** — one deployable, but internal boundaries enforced with Spring Modulith.
3. **Big Ball of Mud** — no enforced boundaries, fastest to start.

---

## Decision

We will build a **modular monolith** using **Spring Modulith**.

Each domain (`identity`, `ledger`, `budgeting`, `funds`, `portfolio`, `notification`) lives in its own Java package under `in.zenvest`. Modules expose a public `api/` sub-package (DTOs, interfaces, Spring events) and keep everything else in `internal/` (package-private where possible). Cross-module communication uses Spring Application Events, never direct repository access across modules.

ArchUnit and Spring Modulith's `ApplicationModules.verify()` enforce these boundaries in CI.

---

## Consequences

**Positive:**
- Single deployable JAR — trivial to operate on one Oracle Cloud Ampere A1 VM.
- One database transaction spans multiple modules when needed (e.g., ledger + notification outbox).
- Shared Testcontainers setup; no inter-service networking to manage in tests.
- Clear migration path: each module can be extracted into a microservice later by promoting its `api/` package to a shared library.

**Negative:**
- All modules scale together — not an issue at v1 traffic.
- A bug in one module can crash the whole app — mitigated by defensive coding and health checks.
- Shared schema — mitigated by schema ownership rules (each module owns its own tables; Flyway migrations are tagged by module).

