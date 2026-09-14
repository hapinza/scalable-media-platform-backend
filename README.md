![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![Redis](https://img.shields.io/badge/Redis-Streams%20%26%20Caching-red)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue)
![Docker](https://img.shields.io/badge/Docker-Container-blue)
![k6](https://img.shields.io/badge/k6-Load%20Testing-purple)
![CI](https://img.shields.io/badge/GitHubActions-CI-black)

# Scalable Media Platform Backend

A Spring Boot backend focused on **event-driven processing, failure recovery, concurrency, and reliability under infrastructure failures**.

The project uses controlled failure injection and load testing to validate how asynchronous workflows behave when publishers, consumers, or downstream dependencies fail.

---

## Quick Start

### Prerequisites

- Docker Desktop
- Git
- k6 *(optional — only required for load tests)*

Clone the repository:

```bash
git clone https://github.com/hapinza/scalable-media-platform-backend.git
cd scalable-media-platform-backend
```

Start the application:

```bash
docker compose up -d --build
```

Verify the containers:

```bash
docker compose ps
```

The core services should be running:

```text
app      Up
db       Up (healthy)
redis    Up
```

Test the asynchronous view-event ingestion path:

```bash
curl -i -X POST http://localhost:8080/analytics/views/1
```

Expected response:

```text
HTTP/1.1 202
```

Optional smoke test:

```bash
k6 run smoke-test.js
```

---

## Key Results

| Experiment | Result |
|---|---|
| **Database outage recovery** | 9,657 events accepted with **0 HTTP failures** while MySQL was unavailable; Redis backlog fully recovered after restoration |
| **Transactional Outbox recovery** | 1,000 requests under 50-VU load; 991 committed and **991/991 recovered from `PENDING → SENT`** |
| **Rate limiting** | 2,591 requests; 2,531 correctly throttled with HTTP 429; **32.67ms p95** |
| **Failed-event recovery** | 10 simulated processing failures; 7 recovered and 3 transitioned to `DEAD` |

---

# Architecture

The project uses two different asynchronous reliability strategies for two different failure modes.

## Transactional Outbox Path

```text
Client
  ↓
Movie Like API
  ↓
MySQL Transaction
  ├─ movie_like
  └─ outbox (PENDING)
       ↓
Outbox Poller
       ↓
Redis Stream
       ↓
Aggregation Consumer
       ↓
Redis Read Model
```

The business update and its corresponding Outbox event are committed in the **same MySQL transaction**.

This protects against the dual-write problem where database state commits successfully but event publication fails.

---

## Redis-First Ingestion Path

```text
Client
  ↓
POST /analytics/views/{movieId}
  ↓
Redis Stream
  ↓
Consumer Group
  ↓
View Event Consumer
  ↓
MySQL
```

View-event ingestion is intentionally decoupled from immediate MySQL availability.

The API first writes the event to Redis Streams, allowing request ingestion to remain available during a temporary database outage.

---

# Database Outage Recovery

To test downstream failure handling, I intentionally stopped only the MySQL container while keeping the application and Redis running.

```text
Application    ✅ Running
Redis          ✅ Running
MySQL          ❌ Stopped
```

During the outage, the API continued accepting events into Redis Streams.

### Outage Result

The test session accepted:

```text
1 manual probe
+ 2,540 requests
+ 7,116 requests
----------------
9,657 total events
```

HTTP failures:

```text
0
```

I then inspected the Redis consumer-group state directly.

```text
Pending: 42
Lag:     9,615
----------------
Total:   9,657
```

All accepted events were still accounted for in Redis.

### Pending vs. Lag

`Pending` represents events that were already delivered to a consumer but had not been acknowledged.

```text
Redis
  ↓
consumer-1
  ↓
DB persistence attempted
  ↓
MySQL unavailable
  ↓
No ACK
  ↓
PENDING
```

`Lag` represents events that remained in the stream and had not yet been delivered to the consumer.

This distinction made it possible to verify not only that requests succeeded, but also where every accepted event was waiting inside the asynchronous pipeline.

---

## Database Restoration

After MySQL was restored, the consumer resumed processing.

Consumer-group lag progressively drained:

```text
9,615
→ 9,495
→ 8,975
→ 7,835
→ 6,325
→ 5,105
→ 705
→ 0
```

However, reaching `lag = 0` did **not** mean recovery was complete.

Redis still reported:

```text
pending = 97
```

Those messages had already been delivered before the database recovered, but they had never been acknowledged.

That exposed a bug in the recovery implementation.

---

## Recovery Bug Discovered During Failure Injection

The original recovery scheduler queried pending messages only for:

```text
recovery-consumer
```

But the failed messages were still owned by:

```text
consumer-1
```

So the original logic effectively behaved like this:

```text
consumer-1
  └─ 97 pending messages

recovery-consumer lookup
  └─ 0 messages found
```

Redis had preserved the events correctly, but the recovery worker could not discover them.

### Fix

The recovery scheduler was changed to inspect pending entries across the **entire consumer group**.

```text
Consumer Group
      ↓
Find stale pending messages
      ↓
XCLAIM
      ↓
recovery-consumer
      ↓
processMessage(...)
      ↓
Database persistence
      ↓
ACK
```

The recovery consumer is now used as the **claim target**, rather than as the pending-message lookup scope.

After the fix:

```text
Pending
97
→ 37
→ 0
```

Final consumer-group state:

```text
lag     = 0
pending = 0
```

This validated the complete outage lifecycle:

```text
MySQL unavailable
      ↓
API remains available
      ↓
Redis buffers events
      ↓
failed processing remains unacknowledged
      ↓
MySQL restored
      ↓
new backlog drains
      ↓
stale pending entries reclaimed
      ↓
reprocessed
      ↓
ACK
```

---

# Consumer Recovery Design

Redis Streams keeps unacknowledged messages in the Pending Entries List, but abandoned messages are not automatically reassigned to another consumer.

The application therefore implements an explicit recovery path.

Normal processing:

```text
MovieViewedConsumer
      ↓
Read new event
      ↓
processMessage()
      ↓
DB success
      ↓
ACK
```

Failure path:

```text
Processing exception
      ↓
No ACK
      ↓
Pending Entry
```

Recovery path:

```text
PendingMessageRecoveryScheduler
      ↓
Group-wide pending lookup
      ↓
Find stale message
      ↓
XCLAIM
      ↓
recovery-consumer
      ↓
processMessage()
      ↓
ACK
```

Normal consumption and recovery reuse the same event-processing logic while keeping their orchestration responsibilities separate.

---

# Transactional Outbox

The Outbox Pattern handles a different problem from the database-outage pipeline.

Without an Outbox:

```text
Business DB commit succeeds
        ↓
Event publication fails
        ↓
Database and event stream diverge
```

Instead, the application writes:

```text
movie_like
+
outbox
```

inside the same transaction.

A scheduled Outbox Poller later publishes `PENDING` events to Redis Streams.

---

## Outbox Failure-Recovery Test

The Outbox Poller was intentionally disabled while concurrent requests were generated.

```text
50 virtual users
1,000 total requests
```

Results:

```text
1,000 requests
      ↓
991 committed
9 connection-pool timeouts
      ↓
991 movie_like rows
991 PENDING outbox events
      ↓
Poller restored
      ↓
991 SENT events
```

The test verified:

- 991 successful business commits
- exactly 991 corresponding Outbox records
- all 991 events recovered from `PENDING → SENT`

The 9 unsuccessful requests were caused by HikariCP connection-pool saturation under concurrent load.

This was a capacity limitation rather than an Outbox consistency failure.

The important invariant was:

```text
committed business transactions
=
corresponding Outbox events
```

---

# Redis Read Model

Movie-like events published through the Outbox pipeline are consumed asynchronously to maintain Redis-backed statistics.

```text
Movie Like API
      ↓
movie_like + outbox
      ↓
Outbox Poller
      ↓
Redis Stream
      ↓
Aggregation Consumer
      ↓
Redis Hash
```

Statistics can be retrieved through:

```http
GET /movies/{movieId}/stats
```

Example:

```json
{
  "movieId": 123,
  "likeCount": 57
}
```

Redis structure:

```text
movie:{movieId}:stats
  likeCount = N
```

This separates transactional writes from frequently accessed read data and reduces repeated database reads.

---

# Failed Events vs. Processed Events

The system uses two different persistence mechanisms for two different reliability concerns.

## `failed_events`

`failed_events` answers:

> Which events could not be processed successfully, and should they be retried?

A failure record tracks information such as:

```text
eventId
payload
error
retryCount
status
```

Possible states:

```text
FAILED
RECOVERED
DEAD
```

This table supports operational retry and failure tracking.

---

## `processed_events`

`processed_events` answers:

> Has this event already been successfully processed?

Its purpose is idempotency.

For example:

```text
consumer-1
    ↓
business write succeeds
    ↓
consumer crashes before ACK
    ↓
message remains Pending
    ↓
recovery-consumer reclaims it
```

Without idempotency protection, the same business operation could be applied twice.

Tracking processed event IDs allows replayed events to be recognized and prevents duplicate processing.

In short:

```text
failed_events
= events that could not be processed

processed_events
= events that have already been processed
```

---

# Failed Event Recovery

Asynchronous processing failures are tracked separately from Redis pending-message recovery.

The system supports:

```http
POST /admin/failed-events/{id}/retry
```

for a single failed event, and:

```http
POST /admin/failed-events/retry?limit=N
```

for bounded bulk retries.

Controlled failure testing produced:

```text
10 failed events
      ↓
7 RECOVERED
3 DEAD
```

Bounded retries prevent permanently failing events from entering infinite retry loops.

Failure injection used for these tests is intended to be isolated behind test-only configuration rather than enabled in the normal application path.

---

# Rate Limiting

A Redis-backed rate limiter protects selected endpoints from excessive traffic.

The limiter uses an atomic Lua script so counter updates and expiration behavior remain consistent under concurrency.

Configuration:

```text
60 requests / minute
```

k6 test:

```text
5 virtual users
10 seconds
Endpoint: /movies/trending
```

Results:

```text
Total requests: 2,591
HTTP 200:         60
HTTP 429:      2,531
p95 latency:   32.67ms
```

The limiter allowed the configured number of requests and predictably rejected excess traffic.

---

# Authentication & Refresh Token Rotation

The application uses stateless JWT authentication with Spring Security.

Authentication features include:

- JWT access-token validation
- BCrypt password hashing
- authenticated principal extraction
- user-scoped endpoint protection

Refresh tokens are stored as SHA-256 hashes.

Rotation uses a conditional update:

```sql
UPDATE refresh_token
SET token_hash = :newHash,
    expires_at = :newExpiration
WHERE member_id = :memberId
  AND token_hash = :oldHash;
```

Because the previous token hash must still match, concurrent attempts to reuse the same previous refresh token cannot both successfully rotate it.

---

# Why Redis Streams?

Redis Streams was chosen intentionally for the scope of this project.

The application already uses Redis for caching and rate limiting, while Streams provides the messaging primitives needed for the failure-recovery experiments:

- consumer groups
- explicit acknowledgments
- Pending Entries List
- consumer lag inspection
- `XCLAIM`-based recovery

This kept the infrastructure relatively small while still allowing at-least-once processing and consumer recovery to be explored directly.

Kafka would be a stronger choice for systems requiring large-scale partitioned throughput, long-lived event retention, or more advanced distributed event-log capabilities.

For this project's workload and learning goals, Redis Streams provided the required reliability primitives with lower operational overhead.

---

# Engineering Takeaways

### Successful HTTP responses are not enough

The database-outage test initially appeared successful because requests continued succeeding and consumer lag eventually reached zero.

Direct inspection showed that 97 messages were still pending.

Recovery was considered complete only after:

```text
lag = 0
pending = 0
```

### At-least-once delivery requires explicit recovery and idempotency

Redis preserves unacknowledged messages, but the application must decide when and how another consumer reclaims them.

Reprocessing must also be safe against duplicate delivery.

### Failure injection exposes bugs that happy-path testing does not

The MySQL outage exposed a consumer-ownership bug in the pending-message recovery path that normal execution had not revealed.

### Infrastructure limits and consistency guarantees are separate concerns

The Outbox test exposed HikariCP saturation while simultaneously confirming that every successfully committed transaction generated its corresponding event.

---

# Future Improvements

- Export Redis lag, pending-entry, and HikariCP metrics to an observability stack
- Automate database and consumer failure injection in integration tests
- Benchmark horizontal consumer scaling and sustained backlog recovery
