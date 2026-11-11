# When Zero Lag Did Not Mean Recovery

*An investigation of Redis Streams pending messages after a controlled MySQL outage*

**Technical writing sample | Scalable Media Platform Backend**  
**Project:** https://github.com/hapinza/scalable-media-platform-backend

## Purpose and audience

I wrote this case study for engineers reviewing the reliability of an event-driven backend. My goal was to explain a failure that a green HTTP response or a single queue metric could hide, show the evidence that changed my understanding of the system, and distinguish what my experiments verified from what they did not.

## The system and the expectation

The project has two asynchronous workflows. A movie-view API publishes events to Redis Streams, and a consumer later persists those events to MySQL. Separately, movie likes are committed to MySQL along with a transactional Outbox record, which a poller publishes to a Redis Stream.

For view ingestion, the intended benefit was to accept requests during a *temporary MySQL outage*, provided the application and Redis remained available. The sequence was:

```text
HTTP request -> Redis Stream -> consumer group -> MySQL -> ACK
```

An HTTP success meant the event had been published to Redis; it did **not** mean the event had already been persisted in MySQL. I wanted to test whether the consumer could eventually recover all work that accumulated during a database outage.

## Failure injection and observations

I deliberately stopped MySQL while keeping the application and Redis running. In a recorded recovery experiment, the system accepted 9,657 view events: one manual probe followed by runs of 2,540 and 7,116 requests.

During the outage, I inspected two Redis consumer-group metrics:

- **Lag:** entries not yet delivered to the consumer group.
- **Pending:** entries already delivered to a consumer but not yet acknowledged.

At one snapshot, the group showed **9,615 lagging** and **42 pending** messages. Together, those metrics accounted for the 9,657 accepted events in Redis at that point.

After I restarted MySQL, the consumer resumed processing. Lag steadily fell from 9,615 to zero. At first glance, that looked like a successful recovery. But another inspection showed **97 messages still pending**.

That discrepancy mattered: zero lag established only that the group had no remaining *undelivered* entries. It said nothing about entries delivered earlier but still waiting for acknowledgment.

## The assumption that was wrong

My initial expectation was that the recovery scheduler would reclaim those pending messages automatically. Instead, its lookup was scoped to messages owned by `recovery-consumer`.

The abandoned messages were owned by `consumer-1`, which had read them during the outage and could not complete its MySQL writes. The recovery worker therefore found **zero** messages even though the group still had 97 pending entries.

```text
consumer-1:       97 pending entries
recovery-consumer:  0 matching entries in its own lookup
```

The bug was not a lack of Redis retention or a failure of the primary consumer to resume. It was a **mismatch between the ownership of pending work and the scope of the recovery query**.

I changed the scheduler to look up stale pending entries across the **entire consumer group**, then use `XCLAIM` to assign qualifying entries to `recovery-consumer`. The reclaimed messages went through the same processing logic as newly delivered messages, rather than a separate recovery-only implementation.

After the change, the pending count decreased from **97 to 37 to zero**.

This verified that the scheduler could find and acknowledge the previously stranded messages in that experiment. It did **not**, by itself, prove that every accepted event was correctly represented in the final MySQL tables: Redis lag and pending metrics are not a database-level reconciliation.

## A separate check: Transactional Outbox recovery

I tested a different reliability boundary in the movie-like workflow. Here, the application writes the business change and an Outbox event in the **same MySQL transaction**, so a committed change has a corresponding event awaiting publication.

I disabled the Outbox poller and sent 1,000 requests with 50 k6 virtual users. The recorded results were:

| Observation | Result |
| --- | ---: |
| Requests attempted | 1,000 |
| Committed business transactions | 991 |
| Matching `PENDING` Outbox records | 991 |
| HikariCP connection-pool timeouts | 9 |
| Committed Outbox records later marked `SENT` | 991 |

After restoring the poller, all 991 committed Outbox records transitioned from `PENDING` to `SENT`. The nine failed requests reflected connection-pool saturation, not missing Outbox events for committed transactions.

This test supported the **commit-to-publication** property for that workload. It did not establish that all 1,000 requests succeeded, nor did it independently verify the final Redis aggregate. Publication to Redis and updating an Outbox row to `SENT` are also not one atomic cross-system operation, so consumers still need duplicate-safe processing.

## What I learned, and what I would improve

These experiments taught me to define recovery in terms of **which stage of the workflow is complete**. HTTP acceptance, stream delivery, acknowledgment, database persistence, and downstream aggregation are different milestones. A single healthy metric can conceal unfinished work in another stage.

Looking back, I would make the test more reproducible and the evidence more complete. In particular, I would automate MySQL shutdown and restoration in an isolated integration environment; assert that both lag **and** pending counts drain; reconcile accepted event IDs against persisted MySQL records; and repeat the recovery tests for each consumer group. I would also test failures in Redis itself, since the MySQL-outage experiment assumes Redis remains available.
