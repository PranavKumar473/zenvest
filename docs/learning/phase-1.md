# Phase 1 Learning — Shared Kernel & Cross-Cutting Concerns

> **Audience:** MCA student learning production backend engineering.  
> **Phase:** 1 — Shared Kernel, Error Handling, Observability & Architecture Guardrails.

---

## 1. What We Built in Phase 1

In this phase, we implemented the foundational **Shared Kernel** (`in.zenvest.shared`) and cross-cutting configuration components that all future business modules (`identity`, `ledger`, `budgeting`, `funds`, `portfolio`, `notification`) rely on:

| Component | Class / File | Purpose |
|---|---|---|
| **Money Value Object** | `Money.java` | Currency math with exact scale of 2 and `RoundingMode.HALF_EVEN`. Rejects `double`/`float`. |
| **Business Calendar** | `BusinessDate.java` | Indian financial market dates (`Asia/Kolkata`), weekend detection, and previous business day calculation. |
| **Clock Abstraction** | `ClockConfig.java` | Injected `java.time.Clock` bean enabling deterministic unit tests with time travel. |
| **UUIDv7 Generator** | `UUIDv7.java` | RFC 9562 time-ordered UUIDs preventing PostgreSQL B-tree index fragmentation. |
| **Fail-Fast Configuration** | `ZenvestProperties.java` | Typed `@ConfigurationProperties` + `@Validated` refusing to start if secrets equal `change-me` in `prod`. |
| **RFC 9457 Problem Details** | `GlobalExceptionHandler.java` | Sanitized standard JSON errors (`type`, `title`, `status`, `detail`, `code`, `traceId`, `errors[]`). Never leaks SQL or stack traces. |
| **Request Tracing & MDC** | `RequestTracingFilter.java` | Correlation ID tracking via `X-Request-Id`, populating SLF4J MDC with `traceId`, `requestId`, and `userId`. |
| **Idempotency Engine** | `@Idempotent`, `IdempotencyRecord.java` | Prevents duplicate POST execution by caching response hashes by `(user_id, idempotency_key)`. |
| **Rate Limiter** | `Bucket4jRateLimiterService.java` | Token-bucket rate limiting preventing brute-force authentication attacks and API scraping. |
| **Audit Service** | `AuditService.java`, `AuditLog.java` | Tamper-evident operational logging in an isolated `REQUIRES_NEW` transaction. |
| **Database Migration V1** | `V1__shared_kernel.sql` | Flyway migration for `idempotency_keys`, `audit_log`, `shedlock`, `outbox_events`, and `event_publication`. |
| **Architecture Verification** | `ArchitectureTest.java` | ArchUnit + Spring Modulith automated checks preventing architectural degradation. |

---

## 2. Deep Dive: Architectural Decisions & Why We Made Them

### Concept A: Why `BigDecimal` with `HALF_EVEN` instead of `double` or `float`?

**What:** In `Money.java`, all monetary quantities are stored strictly as `BigDecimal` with scale 2 and `RoundingMode.HALF_EVEN` (Banker's Rounding).

**Why:** IEEE 754 floating-point types (`float` and `double`) represent numbers in binary scientific notation ($sign \times mantissa \times 2^{exponent}$). Fractions like `0.10` or `0.05` cannot be represented exactly in binary floating-point, causing rounding errors:
```java
double a = 0.10;
double b = 0.20;
System.out.println(a + b); // Prints: 0.30000000000000004!
```
In fintech systems, accumulating fractional cents across millions of transactions leads to financial discrepancy and regulatory penalties. `BigDecimal` represents decimal numbers in base 10 arbitrarily, eliminating representation errors.

**Why Banker's Rounding (`HALF_EVEN`)?**
Traditional `HALF_UP` (always rounding .5 upwards) introduces a positive statistical bias when summing large collections of rounded numbers. `HALF_EVEN` rounds towards the nearest even number when equidistant (e.g., `2.545 -> 2.54`, `2.555 -> 2.56`), statistically balancing upward and downward roundings.

**Alternative Rejected:** Storing money as primitive `double` or `long` (cents/paise without currency context). While `long paise` avoids floating point errors, a dedicated `Money` value object encapsulates rounding rules, division safeguards, and formatting uniformly across the domain.

---

### Concept B: Why UUIDv7 instead of UUIDv4 or Auto-increment Integer IDs?

**What:** `UUIDv7.java` generates 128-bit identifiers formatted as RFC 9562 Version 7:
- First 48 bits: Unix timestamp in milliseconds.
- Next 4 bits: Version `7` (`0111`).
- Next 12 bits: Random data.
- Next 2 bits: Variant `2` (`10`).
- Final 62 bits: Cryptographic random entropy.

**Why:**
1. **B-Tree Index Locality:** Standard UUIDv4 is completely random. In PostgreSQL, primary keys are indexed with B-Trees. When random UUIDv4s are inserted, new rows are scattered across random index disk pages, forcing constant disk cache evictions (buffer thrashing) as tables grow. UUIDv7 is monotonic (time-ordered), so new inserts append to the right edge of the B-Tree, achieving throughput comparable to integer sequences.
2. **Distributed & Collision-Resistant:** Unlike serial integer sequences (`1, 2, 3...`), UUIDv7 does not leak business metrics (e.g., total registered users cannot be guessed from the URL `/users/1024`) and can be safely generated on any client, worker node, or container without database locks.

**Alternative Rejected:** PostgreSQL serial integers / `BIGINT GENERATED ALWAYS AS IDENTITY`. Rejected because integer IDs expose enumeration vulnerabilities and leak database volume statistics to public users.

---

### Concept C: RFC 9457 Problem Details for HTTP APIs

**What:** All REST errors return a standardized JSON schema adhering to RFC 9457:
```json
{
  "type": "https://zenvest.in/errors/validation-failed",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "code": "VALIDATION_FAILED",
  "traceId": "c43a36b8-1234-5678-9abc-def012345678",
  "errors": [
    { "field": "email", "message": "Email address must be well-formed" }
  ]
}
```

**Why:**
- Traditional error formats are inconsistent across frameworks (sometimes a string, sometimes `{ "error": "msg" }`, sometimes a raw Spring error).
- RFC 9457 gives API consumers a machine-parseable contract with predictable properties (`code`, `detail`, `errors[]`).
- **Security:** Stack traces, internal exception class names, and SQL fragments (`PSQLException: relation does not exist`) are strictly suppressed, preventing information leakage to malicious actors.

**Alternative Rejected:** Default Spring Boot white-label error page or custom ad-hoc error maps (`Map<String, Object>`). Both lack industry standardization and make frontend automated API generation brittle.

---

### Concept D: Automated Architectural Enforcement (ArchUnit & Spring Modulith)

**What:** `ArchitectureTest.java` uses ArchUnit and Spring Modulith's `ApplicationModules.verify()` to validate structural constraints as part of every `./mvnw test` build:
1. REST Controllers must never directly inject or call Spring Data `Repository` interfaces (they must go through module services).
2. No entity or money-related class may declare `double` or `float` fields.
3. Module boundaries cannot have circular dependencies or illegal cross-module internal package accesses.

**Why:** In team environments, architectural design degrades over time ("architectural erosion") when developers take shortcuts under pressure. Writing tests that verify the architecture ensures that bad patterns are stopped by the compiler/CI before they ever merge.

---

## 3. Interview Questions & Answers

### Q1: In Spring Boot, how does Virtual Threads (`spring.threads.virtual.enabled=true`) improve scalability compared to traditional thread-pool concurrency?

**A:**
In traditional Spring Boot (platform threads), each HTTP request binds 1:1 to an operating system thread. An OS thread is expensive: it allocates ~1 MB of stack memory and OS context switches take several microseconds. If a server has 200 worker threads and all 200 are waiting on slow database queries or remote HTTP calls, the server exhausts its thread pool and rejects incoming traffic, even if CPU usage is below 10%.

With Java 21+ Virtual Threads (Project Loom), Spring creates lightweight, JVM-managed threads. When a virtual thread executes a blocking I/O operation (like a JDBC query or Redis command), the JVM unmounts the virtual thread from its underlying OS carrier thread and parks it in memory (~few KB of heap). The carrier thread is immediately free to run other requests. When the I/O completes, the JVM remounts the virtual thread on an available carrier thread. This allows a single Spring Boot application to handle tens of thousands of concurrent I/O-bound requests using standard synchronous, readable code without reactive complexity (WebFlux).

---

### Q2: Why is `spring.jpa.open-in-view=false` essential in production applications?

**A:**
By default, Spring Boot sets `spring.jpa.open-in-view=true` (OSIV). OSIV holds a database connection open throughout the entire lifecycle of an HTTP request, including through view rendering and JSON serialization in the web tier.

This causes two severe production hazards:
1. **Connection Pool Starvation:** If an API endpoint takes 500 ms to serialize a large response or call an external REST service after fetching data, the HikariCP database connection remains locked and idle, preventing other threads from acquiring connections.
2. **N+1 Query Leakage:** When lazy-loaded entity relationships are accessed during Jackson JSON serialization outside of a `@Transactional` service boundary, Hibernate silently executes extra SQL queries on the open connection for each nested object.

Setting `open-in-view=false` forces the database connection to close as soon as the `@Transactional` service method finishes. Any lazy loading outside the service throws a `LazyInitializationException`, forcing developers to explicitly fetch required data in the query using `JOIN FETCH` or DTO projections, resulting in predictable and high-performance queries.

---

### Q3: What is the Idempotency-Key pattern, and how does it prevent duplicate transactions in fintech applications?

**A:**
In distributed systems, networks are inherently unreliable. When a client sends a `POST /api/v1/transactions` request, the server may successfully deduct money and commit the database transaction, but a network blip might prevent the HTTP 200 response from reaching the client. The client (or a mobile app with automatic retry logic) will resend the exact same request. Without protection, this creates duplicate charges or records.

The Idempotency-Key pattern solves this:
1. The client generates a unique UUID (the `Idempotency-Key` header) and sends it with the request payload.
2. The server looks up `(user_id, idempotency_key)` in an `idempotency_keys` table.
3. If not found, the server hashes the request payload, executes the business logic, and records `(user_id, idempotency_key, request_hash, response_status, response_body)` in the same database transaction.
4. If a retry arrives with the **same** key and **same** payload within 24 hours, the server skips execution and immediately returns the cached response.
5. If a retry arrives with the **same** key but a **different** payload, the server returns HTTP 422 Unprocessable Content to flag payload tampering.
