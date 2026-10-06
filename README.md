# Scalable Media Platform Backend

A Spring Boot backend for a Netflix-like media application, covering
movie interactions, view tracking, authentication, and asynchronous
processing.

The project began as an application-building exercise and evolved into
a hands-on investigation of event delivery, database failures, recovery,
and duplicate processing.

## Why I Built This

I initially wanted to build a Netflix-like application, starting with
the backend APIs and an MVC structure.

While implementing the APIs and learning about database connection
pools, I began asking:

- Which operations must finish before responding to the user?
- Which operations can be completed asynchronously?
- If the database becomes unavailable, what happens to accepted events?
- If an event is retried, how can I avoid applying it twice?

These questions led to two different workflows:

| Workflow | Immediate work | Asynchronous work |
| --- | --- | --- |
| View tracking | Publish a view event to Redis Streams | Persist the view event to MySQL |
| Movie likes | Commit the like state and an Outbox event in MySQL | Publish the event and update Redis-backed like statistics |

For view tracking, the goal was to keep ingestion available during a
temporary MySQL outage while the application and Redis remained available.

For movie likes, the goal was to preserve the connection between a
committed business change and its eventual publication for downstream
processing.

I then used load tests and controlled failures to investigate whether
these workflows recovered as intended.

## Why These Technologies?

### Java

Java was already familiar to me, so it let me focus on backend design
while extending my knowledge of the language.

I also wanted to practice organizing the application around classes,
interfaces, and explicit responsibilities. Readability depends on those
design decisions rather than on the language alone.

### Spring Boot

I chose Spring Boot to learn and apply its dependency-injection model
while building a Java backend.

Spring-managed beans let controllers, services, repositories, and
infrastructure components receive their dependencies through injection.
This made their relationships explicit without requiring each class to
construct its own dependencies.

The project also uses Spring's support for HTTP endpoints, security,
scheduled jobs, and transaction management.

### MySQL

I wanted to express data queries and updates explicitly in SQL.

MySQL also supports the transactions and unique constraints used here:

- Committing a movie-like change and its Outbox record together.
- Committing a view event and its processing receipt together.
- Enforcing uniqueness for event IDs and business records.

This was a choice for this project's data model and learning goals,
rather than a claim that relational databases are always preferable to
document databases.

### Redis and Redis Streams

Redis supports caching, rate limiting, and the asynchronous event paths.

Redis Streams provides the primitives I wanted to work with directly:

- Consumer groups.
- Explicit acknowledgments.
- Pending-entry inspection.
- Consumer-group lag inspection.
- `XCLAIM` for reclaiming abandoned messages.

These primitives let me implement and investigate recovery behavior
rather than treating asynchronous processing as a fire-and-forget call.

Because the application already used Redis for caching and rate
limiting, Streams kept the infrastructure small. Kafka would be a
stronger choice for systems that need large-scale partitioned
throughput, long-lived event retention, or more advanced distributed
event-log capabilities. For this project's workload and learning goals,
Redis Streams provided the required primitives with lower operational
overhead.

Redis does not eliminate downstream database work. Moving work off the
request path changes when it runs and what the client waits for.

## Code Ownership and Repository Layout

I developed the application from scratch with AI assistance for coding,
explanations, and debugging. It was not forked from another application
or built by copying a tutorial project.

The application code is separate from the third-party frameworks and
libraries it depends on, including Spring Boot and React.

The name `netflix-clone` reflects the original goal of building a
Netflix-style application. That directory contains the frontend
application; it is not an imported library.

| Path | Purpose |
| --- | --- |
| `diexample/` | Java/Spring Boot backend |
| `netflix-clone/` | React frontend |
| `Dockerfile` | Backend container build |
| `docker-compose.yml` | Local service configuration |
| `smoke-test.js` | Basic view-ingestion smoke test |
| `db-outage-test.js` | Database-outage load-test script |
| `like-outbox-test.js` | Like/Outbox load-test script |
| `rate_limit_test.js` | Rate-limiter load-test script |

The reliability experiments described below focus on the backend.

## Quick Start

### Requirements

- Git.
- Docker with Docker Compose.
- k6, if running the load-test scripts.

```bash
git clone https://github.com/hapinza/scalable-media-platform-backend.git
cd scalable-media-platform-backend
docker compose up -d --build
docker compose ps
```

The workflows described here use the application (`app`), MySQL (`db`),
and Redis (`redis`). The Compose configuration also contains Kafka and
ZooKeeper from earlier messaging exploration.

Send a view event:

```bash
curl -i -X POST http://localhost:8080/analytics/views/1
```

The current controller returns HTTP `200` after the Redis publisher
call completes. This response does not mean downstream MySQL processing
has already completed.

Optional smoke test:

```bash
k6 run smoke-test.js
```

## Architecture

### View Tracking: Redis-First Ingestion

```text
Client
  ↓
POST /analytics/views/{movieId}
  ↓
Redis Stream (stream:movie:viewed)
  ↓
Consumer Group
  ↓
MovieViewedConsumer
  ↓
MySQL (view + processing receipt)
  ↓
ACK
```

1. `AnalyticsController` receives a view-tracking request.
2. `ViewEventService` creates an event with a unique event ID.
3. The publisher writes the event to `stream:movie:viewed`.
4. The API returns without waiting for the consumer's MySQL write.
5. `MovieViewedConsumer` reads the event through its consumer group.
6. The processing service persists the view and its processing receipt.
7. The consumer acknowledges the Stream message after successful
   processing or verification of an already committed event.

If processing fails, the message remains unacknowledged.

`PendingMessageRecoveryScheduler` searches the consumer group's pending
entries, claims stale messages, and sends them through the same
processing method used for new messages.

```text
PendingMessageRecoveryScheduler
  ↓
Group-wide pending lookup
  ↓
Find stale message
  ↓
XCLAIM → recovery-consumer
  ↓
Same processing method as normal consumption
  ↓
ACK
```

Normal consumption and recovery reuse the same processing logic while
keeping their orchestration responsibilities separate.

### Movie Likes: Transactional Outbox

```text
Client
  ↓
Movie Like API
  ↓
MySQL Transaction
  ├─ movie_like
  └─ outbox (PENDING)
       ↓
OutboxPoller
       ↓
Redis Stream (stream:movie:liked)
       ↓
MovieLikeAggregationConsumer
       ↓
Redis Read Model
```

1. `MovieLikeService` updates the like state.
2. It records the corresponding event in the Outbox.
3. Both writes commit in the same MySQL transaction.
4. `OutboxPoller` publishes eligible `PENDING` events to
   `stream:movie:liked`.
5. The poller marks published events as `SENT`.
6. `MovieLikeAggregationConsumer` updates the Redis read model.

Without an Outbox, the business change could commit while event
publication fails, leaving the database and the event stream out of
sync. The Outbox addresses this failure window between committing a
business change and publishing its event.

Publishing to Redis and marking the Outbox row as `SENT` are not one
cross-system atomic operation. A failure between them can cause
republication, so downstream processing must tolerate duplicate events.

### Read Paths

`GET /movies/{movieId}/stats` reads Redis-backed movie statistics:

```json
{
  "movieId": 123,
  "likeCount": 57
}
```

The statistics are stored in a Redis hash:

```text
movie:{movieId}:stats
  likeCount = N
```

This separates transactional writes from frequently accessed read data
and reduces repeated database reads.

`GET /movies/trending` uses a separate path: it checks a Redis cache
and, on a cache miss, queries MySQL for view counts, like counts, and
ranking scores before caching the result.

Persisting an individual view event and calculating a trending list
are separate responsibilities.

## Idempotency and Transaction Boundaries

At-least-once delivery means the same event can be processed more than
once. For example:

```text
consumer-1 reads an event
  ↓
business write succeeds
  ↓
consumer crashes before ACK
  ↓
message remains pending
  ↓
recovery-consumer reclaims and reprocesses it
```

Without a processing receipt, the same business change could be
applied twice.

The two processing paths use receipts in different data stores.

| Path | Processing receipt | Atomic boundary |
| --- | --- | --- |
| View persistence | MySQL `processed_events` record | View write and receipt in one MySQL transaction |
| Like aggregation | Redis `processed:event{eventId}` key | Receipt check, hash update, and receipt creation in one Lua execution |

### MySQL View Processing

`ViewEventPersistenceService` owns the transaction that writes the view
and its processing receipt.

The unique event ID prevents concurrent deliveries from creating
multiple processing receipts. If the view write fails, the receipt
is rolled back with it.

`ViewAggregationService` handles integrity errors after that transaction
has ended. An error is treated as an already processed event only when
both committed records can be verified.

The `view_event` table also has a unique event ID. This already protects
the individual view row from duplicate insertion; the separate receipt
records processing completion explicitly.

### Redis Like Aggregation

`MovieLikeAggregationConsumer` delegates the update to
`RedisAggregationScriptExecutor`.

The Lua script:

1. Checks whether the processing receipt exists.
2. Skips the update if it does.
3. Otherwise changes `likeCount` by the event's delta.
4. Records the processing receipt.

The script returns `1` when the event is applied and `0` when an existing
receipt causes it to be skipped.

Receipts expire after seven days. A replay after expiry can therefore
be applied again.

Lua coordinates the Redis operations; it does not make MySQL writes
part of the same transaction.

## Failure-Recovery Experiments

The following results come from separate recorded experiments.
They should not be interpreted as one combined benchmark or as a
rerun of the recent consumer refactoring.

| Experiment | Recorded result | What it checked |
| --- | --- | --- |
| View ingestion during MySQL outage | 15,031 requests, 0% HTTP failures, approximately 7.3 ms p95 | Ingestion availability during that outage |
| View backlog recovery | 9,657 accepted events; final lag and pending both zero | Event accounting in Redis and backlog/pending recovery |
| Outbox publication recovery | 1,000 requests; 991 committed; 991 transitioned from `PENDING` to `SENT` | Publication recovery for committed Outbox records |
| Failed-event retry | 10 failures; 7 `RECOVERED`, 3 `DEAD` | Retry handling and terminal failure states |
| Rate limiting | 2,591 requests; 60 HTTP 200, 2,531 HTTP 429; 32.67 ms p95 | Enforcement of the configured limit |

The rate-limiter latency includes throttled responses. It is not a
general latency measurement for successful application operations.

### MySQL Outage and the Recovery Bug

I stopped MySQL while leaving the application and Redis running.

```text
Application    Running
Redis          Running
MySQL          Stopped
```

The later recovery experiment accepted:

- 1 manual probe.
- 2,540 requests in one run.
- 7,116 requests in another run.

Total: **9,657 events**.

Two consumer-group metrics describe where those events were waiting:

- **Pending**: delivered to a consumer but not yet acknowledged. Here,
  `consumer-1` had read these events, attempted the MySQL write, failed,
  and therefore did not ACK.
- **Lag**: still in the stream and not yet delivered to any consumer.

At the inspected outage snapshot:

- `pending = 42`
- `lag = 9,615`

Those values accounted for all 9,657 accepted events in Redis.

After MySQL was restored, the consumer resumed and lag drained:

```text
9,615 → 9,495 → 8,975 → 7,835 → 6,325 → 5,105 → 705 → 0
```

But **97 messages were still pending**.

The original recovery implementation searched pending messages owned
by `recovery-consumer`. The abandoned messages were actually owned by
`consumer-1`, so the recovery worker did not find them.

```text
consumer-1
  └─ 97 pending messages

recovery-consumer lookup
  └─ 0 messages found
```

I changed recovery to search pending entries across the entire group,
then use `recovery-consumer` as the target for `XCLAIM`.

The remaining pending count decreased from 97 to 37 to zero.

This exposed an important distinction:

- `lag = 0` means no remaining undelivered backlog according to the
  consumer-group metric.
- It does not mean every delivered event has been acknowledged.

These Redis metrics document backlog recovery. They are not, by
themselves, a complete audit of final database contents.

### Outbox Publication Recovery

I disabled the Outbox poller and generated 1,000 requests using
50 virtual users.

The recorded results were:

- 991 committed business transactions.
- 991 corresponding `PENDING` Outbox records.
- 9 HikariCP connection-pool timeouts.

After restoring the poller, all 991 committed Outbox records
transitioned to `SENT`.

The 9 failed requests came from connection-pool saturation under
concurrent load. That is a capacity limitation, separate from the
consistency property the test was checking:

```text
committed business transactions = corresponding Outbox events
```

This test did not demonstrate that all 1,000 requests succeeded or
independently verify the final Redis aggregate.

## Failed-Event Tracking

`failed_events` records failed processing attempts and supports
administrative retry.

Relevant fields include:

- Event ID and payload.
- Failure reason.
- Retry count.
- Processing status.

Statuses include `FAILED`, `RECOVERED`, and `DEAD`.

Available endpoints:

```text
POST /admin/failed-events/{id}/retry
POST /admin/failed-events/retry?limit=N
```

Administrative retries use the same view-processing service as normal
consumption. Bounded retries move permanently failing events to `DEAD`
instead of retrying them indefinitely.

The two tables answer different questions:

| Table | Question it answers |
| --- | --- |
| `failed_events` | Which events could not be processed, and should they be retried? |
| `processed_events` | Has this event already been processed successfully? |

Failed-event tracking and Redis pending-entry recovery are separate
mechanisms. The administrative retry limit should not be interpreted
as a shared retry budget across every recovery path.

## Rate Limiting

A Redis-backed rate limiter protects selected endpoints. It uses an
atomic Lua script so counter updates and expiration stay consistent
under concurrent requests.

Configuration: **60 requests per minute**.

k6 test against `/movies/trending` with 5 virtual users for 10 seconds:

| Metric | Result |
| --- | --- |
| Total requests | 2,591 |
| HTTP 200 | 60 |
| HTTP 429 | 2,531 |
| p95 latency | 32.67 ms |

The limiter allowed the configured number of requests and rejected the
excess.

## Authentication and Refresh Token Rotation

The application uses stateless JWT authentication with Spring Security:

- JWT access-token validation.
- BCrypt password hashing.
- Authenticated principal extraction.
- User-scoped endpoint protection.

Refresh tokens are stored as SHA-256 hashes, not in plain text.

Rotation uses a conditional update:

```sql
UPDATE refresh_token
SET token_hash = :newHash,
    expires_at = :newExpiration
WHERE member_id = :memberId
  AND token_hash = :oldHash;
```

Because the previous token hash must still match, two concurrent
attempts to rotate the same previous refresh token cannot both succeed.

## Reading the Backend Code

Backend classes are under:

```text
diexample/src/main/java/io/github/catimental/diexample/
```

| Class | Responsibility |
| --- | --- |
| `AnalyticsController` | View-ingestion and trending HTTP endpoints |
| `ViewEventService` | View-event creation and publication |
| `MovieViewedConsumer` | Stream consumption and ACK orchestration |
| `ViewAggregationService` | View-processing entry point and verified duplicate handling |
| `ViewEventPersistenceService` | MySQL transaction for a view and its processing receipt |
| `PendingMessageRecoveryScheduler` | Discovery and reclamation of stale view messages |
| `MovieLikeService` | Like-state changes and Outbox creation |
| `OutboxPoller` | Scheduled publication of pending Outbox events |
| `MovieLikeAggregationConsumer` | Like-message processing and ACK orchestration |
| `RedisAggregationScriptExecutor` | Atomic Redis receipt check and aggregate update |
| `FailedEventRetryService` | Administrative retry and failure-status tracking |
| `TrendingService` | Cached retrieval of ranking results |

## Current Limits and Follow-Up Work

- Recent idempotency changes require build and runtime verification;
  the historical experiments above do not validate those changes.
- Redis-first ingestion depends on Redis availability and durability.
  A MySQL-only outage test does not establish safety under Redis failure.
- Redis receipt expiry limits the duplicate-protection window.
- Event deduplication does not prevent repeated API calls from creating
  different event IDs for the same business action. Like-state
  transitions and cancellation publication need further review.
- Recovery behavior must be verified separately for each consumer
  group; the documented view-recovery test does not validate like
  pending-entry recovery.
- Failure injection used in these tests should stay isolated behind
  test-only configuration.
- Export Redis lag, pending-entry, and HikariCP metrics to an
  observability stack.
- Automate database and consumer failure injection in integration tests.
- Benchmark horizontal consumer scaling and sustained backlog recovery.

## What I Learned

Successful HTTP responses do not establish end-to-end completion.

A queue preserves work only within its configured guarantees, and
recovery still requires application logic that can find abandoned work.

Retries require a clear definition of what has already been processed,
and that definition must be tied to the data being changed.

Infrastructure limits and consistency guarantees are separate concerns:
the Outbox test exposed connection-pool saturation while also confirming
that every committed transaction produced its Outbox event.

The most useful failure test was the one that exposed an incorrect
assumption: the recovery consumer's name was being used as the search
scope instead of the destination for reclaimed messages.
