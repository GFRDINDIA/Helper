# Helper Task Service

> Task management & bidding microservice for the **Helper** marketplace platform.  
> Handles task CRUD, full lifecycle, geo-search, open bidding, and fixed-price acceptance.

## Quick Start

```bash
cd helper-task-service
mvn spring-boot:run    # Starts on port 8082 with H2
```

- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **H2 Console**: http://localhost:8082/h2-console

## API Endpoints

### Task APIs (PRD Section 8.2)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/tasks` | CUSTOMER | Create a new task |
| GET | `/api/v1/tasks?lat=&lng=&radius=&domain=` | Public | Geo-search tasks |
| GET | `/api/v1/tasks/{taskId}` | Public | Get task details |
| PUT | `/api/v1/tasks/{taskId}` | Owner | Update task |
| PUT | `/api/v1/tasks/{taskId}/status` | Various | Update lifecycle status |
| DELETE | `/api/v1/tasks/{taskId}` | Owner/Admin | Cancel task |
| GET | `/api/v1/tasks/my-tasks` | Auth | Get user's tasks |
| GET | `/api/v1/tasks/admin/stats` | ADMIN | Task statistics |

### Bidding APIs (PRD Section 8.3)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/tasks/{taskId}/bids` | WORKER | Submit bid |
| GET | `/api/v1/tasks/{taskId}/bids` | Owner/Worker | Get bids |
| PUT | `/api/v1/bids/{bidId}/accept` | Owner | Accept bid |
| PUT | `/api/v1/bids/{bidId}/reject` | Owner | Reject bid |
| DELETE | `/api/v1/bids/{bidId}` | Worker | Withdraw bid |
| GET | `/api/v1/bids/my-bids` | WORKER | Worker's bids |

## Task Lifecycle

```
POSTED → OPEN → ACCEPTED → IN_PROGRESS → COMPLETED → PAYMENT_DONE → CLOSED
                                  ↓              ↓
                              CANCELLED      DISPUTED → CLOSED
```

## Pricing Models

**Open Bidding**: Customer posts → Workers bid → Customer selects winner  
**Fixed Price**: Customer posts with budget → Worker accepts at listed price

## Geo Search

Uses Haversine formula (H2 dev) / PostGIS ST_DWithin (production) for radius queries.
Default radius: 10km, Max: 50km. Results sorted by distance.

## Architecture

- **Port**: 8082
- **Auth**: Validates JWT tokens from Auth Service (shared secret)
- **DB**: Shared PostgreSQL with PostGIS (same DB as auth service)
- **Domains**: Delivery, Electrician, Plumbing, Construction, Farming, Medical, Education, Logistics, Finance, Household

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `app.task.default-search-radius-km` | 10 | Default geo-search radius |
| `app.task.max-search-radius-km` | 50 | Maximum search radius |
| `app.task.max-bids-per-task` | 20 | Max bids per task |
| `app.task.bidding-window-hours` | 24 | Bidding window duration |

## License

Proprietary - Grace and Faith Research and Development Private Limited
