![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![Docker](https://img.shields.io/badge/Docker-Container-blue)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue)
![CI](https://img.shields.io/badge/GitHubActions-CI-black)


# Scalable Media Platform Backend

A production-style Spring Boot backend built to explore
event-driven processing, failure recovery, concurrency, and scalability.

The system combines transactional MySQL writes, the Outbox Pattern,
Redis Streams, idempotent consumers, Redis-backed read models,
rate limiting, and load testing with k6.

## Key Results

| Experiment | Result |
|---|---|
| Database outage recovery | 15,031 requests, 0% HTTP failure, ~7.3ms p95 |
| Transactional Outbox recovery | 1,000 requests under 50-VU load; 991 committed, and all 991 recovered from `PENDING → SENT` |
| Rate limiting | 2,591 requests, 2,531 correctly throttled, 32.67ms p95 |
| Failed-event recovery | 7/10 recovered, 3 transitioned to `DEAD` |
| Read-model architecture | Redis Streams → idempotent consumer → Redis Hash aggregation |

## Why this project matters
This project focuses on understanding how backend systems behave under real-world traffic and designing solutions for scalability, performance, and reliability.

---

## Overview
This project simulates a production-style backend system for a streaming platform, focusing on scalability, performance, and real-world system behavior under load.

### The system focuses on:

- Object-oriented layered architecture
- JWT-based stateless authentication
- Redis-backed rate limiting
- Transactional database design
- CI automation and containerized deployment

---

## System Architecture

```
Client
  ↓
API
  ↓
MySQL Transaction
 ├─ movie_like
 └─ outbox
      ↓
 Outbox Poller
      ↓
 Redis Stream
      ↓
 Aggregation Consumer
      ↓
 Redis Read Model
      ↓
 Movie Stats API

```


### Architecture Design

- Rate limiting is implemented at the filter layer to intercept abusive traffic before reaching business logic.
- Stateless JWT authentication removes server-side session dependency.
- Layered architecture separates concerns between controller, service, and persistence layers.

---

### Authentication & Security Design

- JWT-based stateless authentication
- Access token validation via custom filter
- BCrypt password hashing
- Authentication principal extraction for user-scoped operations
- Secured API endpoints using Spring Security filter chain

---

## Refresh Token Rotation

To improve authentication security, the system implements **refresh token rotation with conditional database updates**.

Key characteristics:

- Refresh tokens are stored as **SHA-256 hashes** in the database
- Each user maintains **a single refresh token record**
- Every refresh request **rotates the token**
- Previous refresh tokens are **immediately invalidated**

Token rotation is implemented using a conditional update:

UPDATE refresh_token
SET token_hash = :newHash,
    expires_at = :newExp
WHERE member_id = :memberId
AND token_hash = :oldHash

This ensures that **only one refresh request can succeed**, preventing replay attacks and concurrent token reuse.

Security benefits:

- Prevents refresh token replay attacks
- Ensures single active refresh token per user
- Protects against concurrent refresh race conditions

Prevented concurrent refresh replay using conditional database rotation.


## Event Processing & Failure Recovery

To ensure reliability in asynchronous event processing, the system implements idempotent handling and a bounded retry recovery workflow.

### Outbox Pattern & Consistency Guarantee

To ensure consistency between database state and event publication, the system implements the Outbox Pattern with a polling-based publisher.

Instead of directly publishing events after a database write, events are first stored in an `outbox` table within the same transaction. A scheduled poller then reads pending events and publishes them to Redis Streams.

To validate reliability, I simulated a failure scenario by temporarily disabling the outbox poller while generating concurrent requests, allowing events to accumulate in the `PENDING` state. After re-enabling the poller, the system successfully published all pending events.

**Results:**

- Sent 1,000 requests under 50-VU concurrent load
- 991 requests completed successfully; 9 timed out due to HikariCP connection-pool saturation
- Verified exactly 991 `movie_like` rows and 991 corresponding `PENDING` outbox events
- Re-enabled the Outbox Poller and recovered all 991 committed events
- Verified complete `PENDING → SENT` transition with no loss among committed transactions

  
The 9 failed requests never completed their database transaction because the
10-connection HikariCP pool was saturated under peak load. This was an
infrastructure capacity limit rather than an Outbox consistency failure:
every successfully committed request produced a corresponding Outbox event.


### Redis Read Model Aggregation

To support fast read operations without repeatedly querying the database, the system implements an event-driven Redis read model using Redis Streams and asynchronous aggregation consumers.

After movie-like events are committed to the database, corresponding outbox events are published to Redis Streams. A dedicated aggregation consumer processes these events and updates Redis Hash-based movie statistics.

Example:

GET /movies/{movieId}/stats

Response:

{
  "movieId": 123,
  "likeCount": 57
}


Flow:

MovieLike API
→ movie_like table + outbox table
→ Outbox Poller
→ Redis Stream
→ Aggregation Consumer
→ Redis Read Model

Redis read model structure:

movie:{movieId}:stats
  likeCount = N

Key characteristics:

- Event-driven aggregation using Redis Streams
- Movie-level like count tracking
- Redis Hash-based read model for fast stat retrieval
- Reduced database reads by serving movie statistics directly from Redis
- Eventual consistency between transactional data and cached read models
- EventId-based idempotent processing to prevent duplicate aggregation updates

This design separates the write path from the read path, reducing database load while maintaining consistency through durable outbox events.



### Database Outage Recovery Experiment

To validate resilience under downstream database failures, I simulated a MySQL outage while sending burst traffic to the view-event ingestion API.

During the outage, the API continued accepting events by writing them to Redis Streams instead of depending on immediate database writes. After the database was restored, the consumer resumed processing the backlog and persisted the pending events to MySQL.

**Results:**

- Sent 15,031 requests during the database outage
- Maintained 0% HTTP request failure during the outage
- Sustained p95 latency around 7.3ms while the database was unavailable
- Recovered and persisted queued events after database restoration
- Used idempotent consumers to prevent duplicate writes during replay

This experiment validated that the ingestion path remained available during database failures and that queued events could be recovered after restoration.


### Idempotent Event Processing

- Ensured safe reprocessing under at-least-once delivery semantics
- Designed idempotent operations to prevent duplicate writes and inconsistent state
- Eliminated race conditions in concurrent environments through atomic processing logic

### Failure Recovery Workflow

- Failed events are persisted in a dedicated `failed_events` table
- Each event tracks retry count and processing status (`FAILED`, `RECOVERED`, `DEAD`)
- Implemented manual replay endpoint:
  - `POST /admin/failed-events/{id}/retry`
- Implemented bulk retry endpoint:
  - `POST /admin/failed-events/retry?limit=N`

### Recovery Results

- Simulated 10 failed events in the event pipeline
- Recovered **7 events (70%)** through controlled retries
- Remaining **3 events were marked as DEAD** after exceeding retry thresholds
- Prevented infinite retry loops through bounded retry policies


## Rate Limiting

### Design Goal

To protect the system from excessive traffic and abuse while maintaining predictable performance under concurrent load.

### Implementation Strategy

- Redis-backed request counter
- Atomic increment operations for concurrency safety
- Client IP extraction via `X-Forwarded-For`
- Filtering before controller execution

### Future Enhancements

- Sliding window algorithm
- Token bucket implementation
- Monitoring request metrics

### Rate Limiting Performance

Implemented a Redis-backed rate limiter (60 req/min) using an atomic Lua script to ensure correct TTL behavior under concurrency.

Load tested with k6 (5 VUs, 10s) against `/movies/trending`.

Results:
- Total requests: 2,591
- HTTP 200: 60
- HTTP 429 (throttled): 2,531
- p95 latency: 32.67ms

Under concurrent load, the system maintained sub-33ms p95 response time while predictably throttling excess requests with HTTP 429.

---

## Database Design

- Relational schema modeling using MySQL
- Transactional operations for consistency
- SQL optimization and N+1 problem exploration (planned)

---

## DevOps & Tooling

- Dockerized multi-container setup (App + MySQL + Redis)
- GitHub Actions CI pipeline for automated builds and tests
- Unit and integration testing using JUnit

---

## What I Learned

- How backend systems behave under concurrent load
- Importance of rate limiting and traffic control
- Trade-offs between caching and database consistency
- Designing systems for scalability and reliability

---

## Performance Experiments (Planned)

- Connection pool monitoring
- Intentional N+1 query simulation and resolution
- Cache introduction and latency comparison
- JWT expiration strategy testing
