# Interview Master Guide — Phase 0 & Phase 1 (Zenvest Backend)

> **Goal:** How to explain the foundational architecture to a tech lead or interviewer like a seasoned engineer—concise, human, zero fluff, zero fumbling, with complete clarity on architectural trade-offs, AI collaboration, and study roadmaps.

---

## 1. The 60-Second Elevator Pitch (What to Say When Asked: "Tell Me About What You Built")

> **Practice saying this out loud (approx. 50–60 seconds):**

> *"In the first phase of Zenvest, I established the shared kernel and architecture guardrails for a modular monolith. Rather than jumping straight into business logic, I set up the cross-cutting concerns that every production fintech system needs.*
> 
> *First, for domain integrity, I built an immutable `Money` value object using `BigDecimal` with scale 2 and Banker's Rounding (`HALF_EVEN`) to eliminate binary floating-point drift, and used monotonic UUIDv7 for primary keys to prevent PostgreSQL B-tree index fragmentation.*
> 
> *Second, for resilience and security, I configured an RFC 9457 `ProblemDetail` global exception handler that suppresses internal stack traces and SQL details, an MDC-based request correlation filter (`X-Request-Id`), an `Idempotency-Key` engine to prevent duplicate financial mutations, and token-bucket rate limiting via Bucket4j.*
> 
> *Finally, I set up automated architectural enforcement using ArchUnit and Spring Modulith to ensure controllers never touch repositories directly and internal module packages remain encapsulated. The entire foundation is validated with Testcontainers against a real PostgreSQL 16 instance and enforced with an 80% JaCoCo line coverage gate."*

---

## 2. What's Necessary vs. What to Skip (Avoid Fumbling)

| Topic | 🗣️ What's Necessary (Say This) | 🤫 What to Skip (Don't Rambling) |
|---|---|---|
| **Tech Stack** | "Spring Boot 4.1 on Java 25 with Virtual Threads enabled, backed by PostgreSQL 16 and Flyway." | Don't list every Maven plugin or detail minor version numbers unless asked. |
| **Money Handling** | "Financial arithmetic must never use `double` or `float` due to binary base-2 representation errors. We use `BigDecimal` with Banker's Rounding." | Don't explain IEEE 754 math formulas unless they ask for the physics of floats. |
| **Database Keys** | "UUIDv7 provides time-ordered 128-bit IDs, which maintains sequential disk locality in PostgreSQL B-Trees while remaining non-enumerable." | Don't recount RFC bit-level shifts unless specifically asked for the bit layout. |
| **Error Handling** | "RFC 9457 standardizes error payloads (`code`, `detail`, `traceId`) while preventing stack traces or database leakages to the client." | Don't list every single HTTP status code. |
| **Idempotency** | "POST mutations can be retried safely using an `Idempotency-Key` header; cached responses prevent double deductions." | Don't talk about frontend button double-clicking; talk about network timeout retries. |

---

## 3. High-Frequency Interview Questions & Precise Answers

### Q1: "Why did you build a Modular Monolith instead of Microservices?"
**Human Answer:**
> *"At v1 scale, microservices introduce distributed system problems—network latency, distributed transactions, partial failures, and heavy container overhead—without any of the scaling benefits. A modular monolith using Spring Modulith gives us strict bounded contexts, private internal packages, and domain events. If a specific module like `funds` ever needs independent scaling down the road, the boundary is already clean and can be extracted into an independent microservice with zero domain refactoring."*

---

### Q2: "Why use UUIDv7 instead of standard UUIDv4 or auto-incrementing serial IDs?"
**Human Answer:**
> *"Auto-incrementing integers leak business volume—an attacker can see order or user counts simply by inspecting sequential IDs (`/users/100` vs `/users/101`). UUIDv4 solves enumeration, but its randomness creates random I/O writes across PostgreSQL B-tree leaf pages, degrading index performance as tables grow. UUIDv7 combines a 48-bit millisecond timestamp prefix with cryptographic randomness. This ensures sequential append locality on disk while keeping IDs distributed, non-guessable, and safe to generate on client or worker nodes."*

---

### Q3: "Why did you choose Banker's Rounding (`HALF_EVEN`) over standard rounding (`HALF_UP`)?"
**Human Answer:**
> *"Traditional round-half-up always rounds .5 upward, which introduces a cumulative positive bias when calculating thousands of ledger entries or tax lines. Round-half-even rounds to the nearest even integer when equidistant—for example, 2.545 becomes 2.54, but 2.555 becomes 2.56. Statistically, half round up and half round down, keeping aggregations neutral."*

---

### Q4: "How does your global exception handler prevent information leakage?"
**Human Answer:**
> *"In production, returning unhandled exception messages or stack traces can expose table names, column names, library versions, or SQL queries to attackers. In our `@RestControllerAdvice`, domain errors map to custom error codes (like `DEMO_READ_ONLY` or `VALIDATION_FAILED`), while unexpected runtime exceptions return a generic message: 'An unexpected internal error occurred'. The actual exception and stack trace are logged internally with a correlation `traceId`, which is also returned in the RFC 9457 ProblemDetail so customer support can locate the exact log line in Alloy or Grafana without exposing internal details to the user."*

---

### Q5: "What is ArchUnit and why run it in your test suite?"
**Human Answer:**
> *"Code reviews catch architectural violations inconsistently. ArchUnit allows us to treat architectural rules as unit tests. For example, we assert that no `@RestController` can depend on a Spring Data `Repository`, and that no money field can be declared as `double` or `float`. If someone introduces a circular dependency or breaks layer isolation, the Maven build fails before the PR can merge."*

---

## 4. How to Answer: "Did You Use AI to Build This?"

Be authentic, transparent, and confident. Senior engineers use modern tooling effectively.

> **Exact response to practice:**
> 
> *"Yes, absolutely. I used AI as an interactive senior mentor and pair programmer—similar to having a senior engineer sitting next to me reviewing PRs and challenging my design decisions.*
> 
> *I used it to bounce architectural trade-offs—like deciding between transactional outbox versus Kafka, verifying Spring Boot 4 dependency compatibility on Maven Central, and creating strict ArchUnit rules. However, I didn't treat it as a code generator where I blindly paste code. I drove the architecture, verified every dependency against Maven Central, wrote test-first validations, and ensured every line followed fintech regulations (like SEBI disclaimers and India DPDP data minimisation)."*

---

## 5. Why You Switched Between Models (Claude vs. Gemini)

If asked why you used Claude at certain points and Gemini at others:

> **Human explanation:**
> 
> - **Claude (Sonnet / Opus):** *"I used Claude when I needed deep architectural reasoning, strict structural discipline, and nuanced trade-off analysis. Claude excels at adhering strictly to constraints, spotting subtle security and concurrency flaws, and writing comprehensive, production-grade documentation and learning notes."*
> - **Gemini:** *"I used Gemini for high-throughput exploration, rapid command diagnostics, and analyzing terminal outputs or real-time build tool logs quickly. Using both gave me the best of both worlds: deep analytical design alongside fast operational iteration."*

---

## 6. Study Guide & Time Allocation for Upcoming Phases

To master the concepts deeply and be confident in technical interviews, here is your target study roadmap:

```
Total Dedicated Prep Time: 12–15 Hours
├── Phase 1 Mastery (4 Hours)
├── Phase 2: Identity & Security Prep (5 Hours)
└── Phase 3–5: Data & Financial Math Prep (4 Hours)
```

### Detailed Topic Breakdown

| Phase / Topic | What to Learn | Time to Devote | Recommended Resources |
|---|---|---|---|
| **1. Value Objects & Clean Architecture** | Immutability, `BigDecimal` scale/rounding, value objects vs entities, UUIDv7 spec (RFC 9562). | **2 Hours** | • *Effective Java* (Item 17: Minimize mutability)<br>• RFC 9562 Specification |
| **2. Spring Modulith & ArchUnit** | Modular architecture, package encapsulation (`api` vs `internal`), architecture verification tests. | **2 Hours** | • [Spring Modulith Reference Docs](https://docs.spring.io/spring-modulith/reference/)<br>• [ArchUnit User Guide](https://www.archunit.org/user-guide/html/000_Index.html) |
| **3. JWT RS256 & Auth Architecture (Phase 2 Prep)** | Symmetric (HS256) vs Asymmetric (RS256), public/private key pairs, Key ID (`kid`) rotation, refresh token families. | **3 Hours** | • [Auth0 JWT Handbook](https://auth0.com/resources/ebooks/jwt-handbook)<br>• Spring Security OAuth2 Resource Server documentation |
| **4. Password Hashing & Rate Limiting** | Bcrypt work factor (cost 12), timing attacks, common password dictionaries, Bucket4j token bucket algorithm. | **2 Hours** | • OWASP Password Storage Cheat Sheet<br>• [Bucket4j Documentation](https://bucket4j.com/) |
| **5. Database Indexing & Keyset Pagination (Phase 3 Prep)** | B-Tree indexing, composite indexes `(user_id, occurred_on DESC, id DESC)`, cursor-based pagination vs offset pagination. | **2 Hours** | • *Use The Index, Luke!* (Marcus Winand)<br>• Vlad Mihalcea's Hibernate & PostgreSQL guides |
| **6. Financial Math & Resilient HTTP (Phase 5 Prep)** | CAGR, Rolling Returns, Sharpe & Sortino ratios, Resilience4j circuit breakers & retries. | **2 Hours** | • Investopedia (Sharpe, Sortino, CAGR formulas)<br>• Resilience4j official documentation |

---

## 7. Quick Flashcard Cheatsheet (Memorize Before Interviews)

- **`spring.threads.virtual.enabled=true`** → Java 25 virtual threads unmount on blocking I/O, allowing high concurrency without reactive syntax.
- **`spring.jpa.open-in-view=false`** → Closes DB connection after transaction ends, preventing pool exhaustion and hidden N+1 queries during JSON serialization.
- **Banker's Rounding (`HALF_EVEN`)** → Statistically unbiased rounding towards nearest even number for equidistant values.
- **UUIDv7** → Monotonic, timestamp-prefixed UUID preventing B-Tree page splits and disk fragmentation.
- **RFC 9457** → Standard problem details JSON payload with machine-readable `code` and sanitized user-facing message.
- **ShedLock** → Distributed lock using database row state to ensure scheduled jobs execute on only one instance at a time.
- **Idempotency Key** → Unique client token preventing duplicate financial records when network retries occur.
