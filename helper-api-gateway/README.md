# Helper API Gateway

> Single entry point for all **Helper** marketplace microservices. Built with Spring Cloud Gateway (reactive/Netty). Provides JWT validation, route-based forwarding, rate limiting, CORS, circuit breakers, and request tracing.

## Quick Start

```bash
cd helper-api-gateway
mvn spring-boot:run    # Starts on port 8080
```

All requests go through `http://localhost:8080` and are routed to the correct microservice.

## Architecture

```
Flutter App / React Web / Admin Panel
              │
              ▼
    ┌─────────────────┐
    │  API Gateway     │  :8080
    │  (Spring Cloud)  │
    └────────┬────────┘
             │  JWT validated + forwarded as X-User-Id/X-User-Role headers
             │
    ┌────────┼────────────────────────────────┐
    │        │        │        │       │       │
    ▼        ▼        ▼        ▼       ▼       ▼
  Auth    Task     User    Payment Rating  Notification
  :8081   :8082    :8083    :8084  :8085    :8086
```

## Route Map

All routes use prefix-based matching. The gateway forwards the full path to the downstream service.

| Prefix | Downstream Service | Port |
|--------|-------------------|------|
| `/api/v1/auth/**` | Auth Service | 8081 |
| `/api/v1/tasks/**` | Task Service | 8082 |
| `/api/v1/profiles/**`, `/api/v1/workers/**`, `/api/v1/customers/**` | User Profile Service | 8083 |
| `/api/v1/kyc/**` | User Profile Service | 8083 |
| `/api/v1/payments/**`, `/api/v1/ledger/**` | Payment Service | 8084 |
| `/api/v1/ratings/**`, `/api/v1/flags/**` | Rating Service | 8085 |
| `/api/v1/notifications/**`, `/api/v1/devices/**` | Notification Service | 8086 |
| `/api/v1/internal/notify/**` | Notification Service | 8086 |
| `/api/v1/admin/tasks/**` | Task Service | 8082 |
| `/api/v1/admin/kyc/**`, `/api/v1/admin/profiles/**` | User Profile Service | 8083 |
| `/api/v1/admin/payments/**`, `/api/v1/admin/config/**` | Payment Service | 8084 |
| `/api/v1/admin/ratings/**`, `/api/v1/admin/flags/**` | Rating Service | 8085 |
| `/api/v1/admin/notifications/**` | Notification Service | 8086 |

## Features

### 1. JWT Authentication (Global Filter)

Every request (except open paths) must have a valid `Authorization: Bearer <token>` header. The gateway:
- Validates the JWT signature and expiry
- Extracts user claims (userId, role, email)
- Forwards them as request headers to downstream services:
  - `X-User-Id`
  - `X-User-Role`
  - `X-User-Email`
  - `X-Gateway-Validated: true`

Open paths (no JWT needed): `/api/v1/auth/login`, `/api/v1/auth/register`, `/api/v1/ratings/user/**`, `/api/v1/ratings/summary/**`, `/actuator/health`, Swagger docs.

### 2. Rate Limiting

In-memory sliding window rate limiter (Redis-backed in production):
- Authenticated users: **100 requests/minute** (by userId)
- Anonymous: **30 requests/minute** (by IP)
- Response headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`
- Returns `429 Too Many Requests` with `Retry-After: 60`

### 3. Circuit Breaker (Resilience4j)

Each downstream service has its own circuit breaker:
- **Sliding window**: 10 calls
- **Failure threshold**: 50% → circuit opens
- **Wait in open state**: 10 seconds
- **Slow call threshold**: 3 seconds / 80%
- **Fallback**: Returns a user-friendly 503 with service-specific messaging

### 4. CORS

Configured for:
- Flutter mobile app (no CORS restrictions on native)
- React/Next.js web app (`localhost:3000` in dev, `helper.app` in prod)
- Admin panel (`admin.helper.app`)

### 5. Request Logging & Tracing

Every request gets:
- `X-Request-Id` header (8-char UUID) for distributed tracing
- Request/response logging: method, path, origin, status code, latency

### 6. Health Monitoring

| Endpoint | Description |
|----------|-------------|
| `GET /gateway/health` | Gateway health check |
| `GET /gateway/services` | Status of all 6 downstream services |
| `GET /actuator/health` | Spring Boot actuator health |
| `GET /actuator/gateway` | All registered routes |

## Example Requests

```bash
# Register (no JWT needed — open path)
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Test123!","fullName":"Test User","role":"CUSTOMER"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Test123!"}'
# Returns: { "token": "eyJhbG..." }

# Create task (JWT required — routed to Task Service 8082)
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer eyJhbG..." \
  -H "Content-Type: application/json" \
  -d '{"title":"Fix sink","domain":"PLUMBING","budget":500}'

# Check all services
curl http://localhost:8080/gateway/services
```

## Full Platform Docker Compose

The `docker-compose-full.yml` starts the **entire Helper platform**:

```bash
# From a directory containing all service folders:
docker-compose -f helper-api-gateway/docker-compose-full.yml up --build
```

This starts: PostgreSQL (PostGIS), Redis, Auth, Task, User, Payment, Rating, Notification, and API Gateway.

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `APP_JWT_SECRET` | (set in yml) | JWT signing secret (must match Auth Service) |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | Auth service base URL |
| `TASK_SERVICE_URL` | `http://localhost:8082` | Task service base URL |
| `USER_SERVICE_URL` | `http://localhost:8083` | User service base URL |
| `PAYMENT_SERVICE_URL` | `http://localhost:8084` | Payment service base URL |
| `RATING_SERVICE_URL` | `http://localhost:8085` | Rating service base URL |
| `NOTIFICATION_SERVICE_URL` | `http://localhost:8086` | Notification service base URL |
| `REDIS_HOST` | `localhost` | Redis for rate limiting (prod) |

## Testing

```bash
mvn test    # Runs GatewayJwtTest (7 test cases)
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Cloud Gateway (reactive/Netty) |
| Auth | JWT validation (jjwt 0.12.5) |
| Rate Limiting | In-memory (dev), Redis (prod) |
| Circuit Breaker | Resilience4j |
| Tracing | X-Request-Id header |
| Port | 8080 |

## License

Proprietary - Grace and Faith Research and Development Private Limited
