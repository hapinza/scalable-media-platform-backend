# Streaming Platform Backend Architecture Simulation

## Overview

This project simulates a production-style backend system for a streaming platform.

The goal is not only to replicate features, but to design and test backend architecture principles such as authentication, rate limiting, database performance optimization, and system reliability under load.

### The system focuses on:

- Object-oriented layered architecture
- JWT-based stateless authentication
- Redis-backed rate limiting
- Transactional database design
- CI automation and containerized deployment

---

## System Architecture

```
Client (React)
    |
    v
Spring Security Filter Chain
    |
    v
JWT Authentication Filter
    |
    v
Rate Limiting Filter (Redis)
    |
    v
Controller Layer
    |
    v
Service Layer
    |
    v
JPA / MySQL
```


### Architecture Design

- Rate limiting is implemented at the filter layer to intercept abusive traffic before reaching business logic.
- Stateless JWT authentication removes server-side session dependency.
- Layered architecture separates concerns between controller, service, and persistence layers.

---

## Authentication & Security Design

- JWT-based stateless authentication
- Access token validation via custom filter
- BCrypt password hashing
- Authentication principal extraction for user-scoped operations
- Secured API endpoints using Spring Security filter chain

---

## Rate Limiting (In Progress)

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

## Performance Experiments (Planned)

- Connection pool monitoring
- Intentional N+1 query simulation and resolution
- Cache introduction and latency comparison
- JWT expiration strategy testing
