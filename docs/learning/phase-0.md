# Phase 0 Learning — Repository Guardrails

> **Audience:** MCA student learning backend engineering.
> **Phase:** 0 — Repository Guardrails (no business logic yet).

---

## What We Did in Phase 0

We set up the "safety net" for the entire project before writing a single line of Java:

| File / Tool | Purpose |
|---|---|
| `.gitignore` | Tells Git which files to never track (build outputs, secrets, IDE files) |
| `.editorconfig` | Enforces consistent code formatting across all editors |
| `.gitattributes` | Normalises line endings so the project works on Mac, Windows, and Linux |
| `.gitleaks.toml` | Configures the secret-detection scanner |
| `CONTRIBUTING.md` | Documents how to set up the pre-commit hook so no developer can accidentally commit a password |
| `backend/.env.example` | Lists every environment variable the backend needs, with safe placeholder values |
| `infra/.env.example` | Lists every environment variable Docker Compose needs, with safe placeholder values |
| `infra/compose.dev.yml` | Local development environment: PostgreSQL 16, Redis 7, Mailpit |
| `.github/workflows/ci.yml` | Runs on every push/PR: builds Java, runs tests, scans for secrets, runs CodeQL |
| `.github/dependabot.yml` | Automatically opens PRs when Maven, npm, Docker, or GitHub Actions have updates |
| `docs/adr/` | Architecture Decision Records: permanent log of *why* we made technical choices |

---

## Concept 1: `.gitignore` — Why Not Track Everything?

**What:** A list of file patterns that Git ignores entirely.

**Why:** Some files should *never* be in version control:
- **Secrets** (`.env`, `*.pem`, `*.key`) — if committed, anyone with repo access gets your database password.
- **Build output** (`target/`, `dist/`, `node_modules/`) — generated automatically; committing them wastes space and causes conflicts.
- **IDE files** (`.idea/`, `.vscode/`) — personal editor config differs between teammates.

**Alternative we rejected:** Tracking everything, then removing sensitive files later.
This is dangerous because `git rm` does not erase history. Even after removal, secrets remain readable with `git log -p`. The correct fix (rewriting git history) is disruptive for teams.

---

## Concept 2: Gitleaks + Pre-Commit Hook — Shift Security Left

**What:** Gitleaks scans your *staged* files before every commit. If it finds anything that looks like an API key, password, or private key, it blocks the commit.

**Why:** The earlier a security issue is caught, the cheaper it is to fix. Catching a leaked secret before it reaches GitHub means no credentials are exposed.

**Alternative we rejected:** Relying on developers to manually check. Humans forget, especially under deadline pressure. The hook is automatic — no memory required.

**How the hook works:**
```
git commit → runs .git/hooks/pre-commit → gitleaks protect --staged
     ↓
   PASS → commit proceeds
   FAIL → commit blocked, prints which file/line triggered it
```

---

## Concept 3: `.env.example` — The Twelve-Factor App Pattern

**What:** A committed example file listing every environment variable with a placeholder value (`change-me`). The real `.env` file (with real secrets) is git-ignored and lives only on each developer's machine or on the server.

**Why:** This follows [12-Factor App](https://12factor.net/) methodology (Factor III: Config). The app reads all configuration from environment variables, not from hardcoded values in source code. Benefits:
- One codebase can run in dev, staging, and production with different configs.
- Secrets never enter version control.
- New team members know exactly what variables to set.

**Alternative we rejected:** Storing config in `application.properties` directly with real values. This is a security and portability failure.

---

## Concept 4: Architecture Decision Records (ADRs)

**What:** Short Markdown documents that record *why* a technical decision was made, what alternatives were considered, and what the consequences are.

**Why:** Code tells you *what* the system does. ADRs tell you *why* it was built that way. Six months later, when a new developer asks "why are we using Spring Boot instead of FastAPI?", the ADR has the full reasoning.

**Alternative we rejected:** Keeping decisions in people's heads or Slack messages. Both disappear quickly.

**We wrote three ADRs:**
- `0001-modular-monolith.md` — why one deployable vs microservices
- `0002-spring-boot-over-fastapi.md` — why Java vs Python
- `0003-outbox-before-broker.md` — why database outbox vs Kafka

---

## Concept 5: `compose.dev.yml` — Isolated Local Infrastructure

**What:** A Docker Compose file that starts PostgreSQL 16, Redis 7, and Mailpit (email inbox) on your local machine.

**Why:** Every developer gets an identical, disposable environment. No "works on my machine" issues.

**Security note:** Postgres and Redis are bound to `127.0.0.1` only. This means they are only reachable from the same machine, never from the internet.

**Mailpit** is a local email inbox. When the app sends an email verification link, instead of going to a real inbox, it shows up at `http://localhost:8025`. This lets you test email flows without sending real emails during development.

---

## 3 Interview Questions & Answers

### Q1: What is the difference between `.gitignore` and `.gitattributes`?

**A:** `.gitignore` controls *which files* Git tracks — it completely excludes listed files from version control. `.gitattributes` controls *how* Git handles tracked files — for example, normalising line endings (CRLF on Windows, LF on Mac/Linux) so the same file doesn't appear changed when opened on a different OS. Both are needed: `.gitignore` for exclusion, `.gitattributes` for normalisation.

---

### Q2: Why do we store secrets as environment variables instead of in `application.properties`?

**A:** `application.properties` is part of the codebase and tracked in git. If a real password is ever committed there, it enters git history permanently — removing it from the working tree does not remove it from history. Environment variables live outside the codebase: on the developer's machine in a git-ignored `.env` file, or in the server's environment, or in a secrets manager. This separates *code* (which is public) from *configuration* (which is private), following the Twelve-Factor App methodology.

---

### Q3: What is a pre-commit hook and what is the difference between `gitleaks protect --staged` and `gitleaks detect`?

**A:** A pre-commit hook is a shell script Git automatically executes before recording a commit. If it exits with a non-zero code, the commit is aborted. `gitleaks protect --staged` scans only the files you've staged with `git add` — this is fast and prevents secrets from entering even a local commit. `gitleaks detect` scans the full repository history — useful in CI to audit the entire git log. We use `protect --staged` in the pre-commit hook for speed, and `gitleaks detect` in the CI pipeline for full coverage.
