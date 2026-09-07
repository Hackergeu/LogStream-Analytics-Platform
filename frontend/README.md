# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
# LogStream — Distributed Log Analytics & Alerting Platform

A full-stack observability platform for ingesting, indexing, searching, and
alerting on application logs in near real time — built as a from-scratch
implementation of an ELK-style stack (Elasticsearch + Logstash + Kibana),
using an embedded search engine instead of an external one.

---

## Overview

A DevOps engineer needs to find the exact log lines behind a spike in
errors — fast, without waiting on a slow relational database `LIKE` query
across millions of rows. LogStream solves this by combining a
high-throughput gRPC ingestion path with an embedded Apache Lucene search
index, a scheduled alerting engine, and a live-updating React dashboard —
all wired together in a single Spring Boot backend.

**Example query the system is built around:**
```
level:ERROR AND service:billing-api AND response_time_ms:{1000 TO *}
```
This finds every `ERROR`-level log from `billing-api` with a response time
over 1000ms — combining exact-match fields, full-text search, and numeric
range queries in one query string.

### Architecture

```
                    ┌─────────────────┐
  microservices ──▶ │  gRPC Ingestion  │
   (simulated)      │  (port 9090)     │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │  Lucene Index     │◀──── REST Search API
                    │  (embedded)       │        (port 8080)
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐        ┌──────────────┐
                    │  Scheduled        │        │  WebSocket    │
                    │  Alerting Engine  │        │  Live Tail    │
                    └───────────────────┘        └──────┬───────┘
                                                          │
                                                 ┌────────▼─────────┐
                                                 │  React Dashboard  │
                                                 │  (Vite + ECharts) │
                                                 └───────────────────┘
```

Two ingestion paths exist by design: **gRPC** (port 9090) simulates real
microservices pushing logs in, matching the PDF spec's intended
architecture; a lightweight **REST endpoint** (`/api/logs/ingest`) exists
purely as a convenience for generating test data from the dashboard,
without needing a separate gRPC client tool for every test.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 17, Node 20 |
| Backend framework | Spring Boot 4.1 |
| Log ingestion | gRPC (native Spring Boot 4.1 integration) + Protobuf |
| Search & indexing | Apache Lucene 9.11.1 (embedded) |
| Alerting | Spring `@Scheduled` tasks |
| Real-time streaming | WebSocket (Spring `TextWebSocketHandler`) |
| Frontend | React 18 (Vite), ECharts |
| Containerization | Docker, multi-stage builds, Docker Compose |
| Build tools | Maven, npm |

---

## Concepts Covered

- **Contract-first API design** — defining a `.proto` schema before writing
  any service code, and generating client/server stubs from it
- **gRPC service implementation** — unary RPCs, `StreamObserver`, and how
  Spring Boot 4.1's native gRPC starter differs from the older third-party
  `net.devh` starter
- **Lucene indexing fundamentals** — `StringField` vs `TextField` (exact
  match vs. analyzed/tokenized text), and why mixing them without
  understanding the difference silently breaks queries
- **Numeric range queries** — `IntPoint`/`LongPoint` vs. the deprecated
  `IntField`/`LongField`, and why Lucene requires consistent per-field
  schemas across an entire index (including debugging a real
  `doc values type` mismatch caused by a schema change)
- **Custom `QueryParser` extension** — overriding `getRangeQuery()` so
  Lucene's query syntax (`field:{1000 TO *}`) resolves to numeric range
  queries instead of being misinterpreted as text
- **Scheduled background processing** — `@Scheduled` + `@EnableScheduling`
  for periodic rule evaluation, decoupled from the request/response cycle
- **WebSocket session management** — tracking connected clients in a
  thread-safe collection and broadcasting to all of them on an event
- **Multi-stage Docker builds** — separating build-time tooling (Maven,
  npm) from the lean runtime image, and why that separation matters for
  image size and security surface
- **CORS configuration** — why a browser on `localhost:5173` calling an
  API on `localhost:8080` requires explicit `@CrossOrigin` configuration
  even when both are "localhost"

---

## What I Learned

- **Lucene enforces a consistent schema per field across an entire index.**
  Changing how a field is indexed (e.g. switching `timestamp` from a
  deprecated `LongField` to `LongPoint` + `StoredField`) breaks any
  existing index built under the old schema — the fix isn't a code
  change, it's deleting and rebuilding the index. This is the same
  "reindexing" concept that exists in production Elasticsearch clusters.
- **`QueryParser` analyzes every query term the same way by default** —
  including terms meant for exact-match fields. `level:ERROR` silently
  failing to match a stored `"ERROR"` value (because the query text got
  lowercased to `"error"` by the analyzer) was a real, subtle bug — fixed
  with a `PerFieldAnalyzerWrapper` so exact-match fields skip analysis
  entirely.
- **PowerShell's argument quoting is a genuine source of friction** when
  driving CLI tools like `grpcurl` — piping JSON from a file via stdin
  (`-d "@"`) proved far more reliable than trying to escape quotes inline.
- **Spring Boot 4.1 made gRPC a first-party citizen** — no more relying on
  an unmaintained third-party starter; the trade-off of moving off the
  (by-then EOL) Spring Boot 3.5.x line was worth it for long-term support.
- **Browsers cannot speak gRPC directly.** Any browser-facing feature
  (search, dashboard, live tail) needs REST or WebSocket — gRPC's role
  here is specifically for service-to-service ingestion, not client-facing
  APIs.

---

## How to Run

### Option 1: Docker (recommended)

```bash
docker compose up --build
```
- Frontend: http://localhost:5173
- Backend REST API: http://localhost:8080
- Backend gRPC: localhost:9090

### Option 2: Run locally

**Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

### Try it out

1. Open the dashboard and use the **Send Test Log** form to generate a few
   logs (try different levels: `INFO`, `WARN`, `ERROR`)
2. Watch them appear instantly in **Live Tail**
3. Search for them: `level:ERROR AND service:billing-api`
4. Register an alert rule and watch it fire in the **Active Alerts** panel:
   ```bash
   curl -X POST http://localhost:8080/api/alerts \
     -H "Content-Type: application/json" \
     -d '{"name":"High Error Rate","query":"level:ERROR","thresholdCount":1,"windowMinutes":60}'
   ```

---

## Possible Improvements

- **Kafka as a message broker** between ingestion and indexing, to decouple
  the two and allow horizontal scaling of indexer instances independently
  of ingestion throughput
- **Authentication** (JWT-based) to restrict dashboard access and require
  authorized API keys for log ingestion, preventing unauthorized services
  from writing to the index
- **Persistent alert rules** — currently stored in memory and lost on
  restart; would move to a database table
- **Real webhook delivery** for triggered alerts (Slack/PagerDuty
  integration) instead of a server-side log line
- **Pagination** for search results once log volume grows beyond what
  fits comfortably on one page
- **Authenticated WebSocket connections**, since Live Tail currently
  accepts any connection without a token
- **Horizontal scaling** of the Lucene index itself (sharding), since a
  single embedded index has a practical ceiling that a distributed system
  like Elasticsearch is built to exceed

---

## Project Structure

```
LogStream-Analytics-Platform/
├── backend/
│   ├── src/main/java/com/logstream/backend/
│   │   ├── grpc/          # gRPC service implementation
│   │   ├── search/        # Lucene indexing + search services
│   │   ├── alerting/      # Scheduled alert rule evaluation
│   │   ├── websocket/     # Live Tail broadcaster
│   │   └── controller/    # REST endpoints
│   ├── src/main/proto/    # Protobuf schema
│   └── Dockerfile
├── frontend/
│   ├── src/components/    # SearchBar, ResultsTable, VolumeChart,
│   │                      # LogGenerator, AlertsPanel, LiveTail
│   └── Dockerfile
└── docker-compose.yml
```
