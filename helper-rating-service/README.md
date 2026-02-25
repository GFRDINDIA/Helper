# Helper Rating & Feedback Service

> Bidirectional rating system with time-weighted scoring, flagging, and admin moderation for the **Helper** marketplace platform.

## Quick Start

```bash
cd helper-rating-service
mvn spring-boot:run    # Starts on port 8085 with H2
```

- **Swagger UI**: http://localhost:8085/swagger-ui.html
- **H2 Console**: http://localhost:8085/h2-console

## PRD Requirements Implemented (Section 3.6)

| Requirement | Implementation |
|------------|----------------|
| Bidirectional: both parties rate each other | `CUSTOMER_TO_WORKER` and `WORKER_TO_CUSTOMER` rating types |
| 1-5 stars + text feedback | `score` (CHECK 1-5) + `feedback` (TEXT, max 2000 chars) |
| Weighted rating: recent ratings weigh more | Exponential decay: half-life of 180 days (configurable) |
| Rating affects search ranking | `weighted_rating` in `user_rating_summaries` for sort |
| Minimum 5 ratings before public | `is_public` flag, auto-set when `total_ratings >= 5` |
| Flagging system for admin review | `flags` table with reasons, admin review, auto-hide |

## Weighted Rating Algorithm

```
weight = e^(-λ × days_ago)
where λ = ln(2) / decay_days   (half-life model)

weighted_average = Σ(score × weight) / Σ(weight)
```

**Example** (decay_days = 180):
- Rating from today: weight = 1.0
- Rating from 180 days ago: weight = 0.5
- Rating from 1 year ago: weight = 0.25

This means a 5-star review from today has 4× the influence of a 5-star review from a year ago.

## API Endpoints (18 total)

### Rating Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/ratings` | AUTH | Submit a rating (1-5 stars + feedback) |
| GET | `/api/v1/ratings/user/{userId}` | PUBLIC | Ratings received by a user |
| GET | `/api/v1/ratings/summary/{userId}` | PUBLIC | Rating summary (avg, weighted, star breakdown) |
| GET | `/api/v1/ratings/task/{taskId}` | AUTH | All ratings for a task |
| GET | `/api/v1/ratings/my/given` | AUTH | Ratings I've given |
| GET | `/api/v1/ratings/my/received` | AUTH | Ratings I've received |

### Flag Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/flags` | AUTH | Flag inappropriate behavior/rating |

### Admin Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/admin/ratings/stats` | ADMIN | Platform-wide rating & flag stats |
| PUT | `/api/v1/admin/ratings/{id}/hide` | ADMIN | Hide a rating (recalculates summary) |
| PUT | `/api/v1/admin/ratings/{id}/show` | ADMIN | Restore a hidden rating |
| GET | `/api/v1/admin/flags/pending` | ADMIN | Pending flags for review |
| GET | `/api/v1/admin/flags/user/{userId}` | ADMIN | All flags for a user |
| PUT | `/api/v1/admin/flags/{id}/review` | ADMIN | Review flag (dismiss/action + optionally hide rating) |

## Database Schema

### ratings (PRD Section 4.1)
Stores individual ratings: rating_id (PK), task_id, given_by, given_to, score (1-5), feedback, rating_type, is_visible. Unique constraint on (task_id, given_by, given_to).

### flags
Behavior reports: flag_id (PK), rating_id (optional FK), task_id, reporter_id, reported_user_id, reason (enum), description, status (PENDING/REVIEWED/DISMISSED/ACTION_TAKEN), admin_notes, reviewed_by, reviewed_at.

### user_rating_summaries
Precomputed aggregates: user_id (PK), average_rating, weighted_rating, total_ratings, star counts (1-5), total_flags_received, is_public (true when total >= 5).

## Key Features

**Duplicate Prevention**: One rating per direction per task. Unique constraint on (task_id, given_by, given_to). Customer can rate worker AND worker can rate customer, but each only once.

**Auto-Hide**: When a user accumulates 3+ pending flags (configurable), their flagged rating is automatically hidden. Admin can restore it after review.

**Summary Recalculation**: Every time a rating is submitted, hidden, or shown, the rated user's `user_rating_summaries` row is fully recalculated (simple + weighted average, star distribution, public threshold check).

**Public Threshold**: Workers need 5+ ratings before their score appears publicly. This prevents a single bad review from unfairly destroying a new worker's reputation.

## Sample Dev Data

- **Worker 1**: 6 ratings (5,4,5,5,4,5) → avg 4.67★, IS PUBLIC
- **Worker 2**: 2 ratings (3,1) → avg 2.00★, NOT PUBLIC (below threshold)
- **Customer 1**: 2 ratings from Worker 1 (5,4) → avg 4.50★
- **1 Pending Flag**: Worker 2 flagged Customer 1's 1-star review as FAKE_REVIEW

## Architecture

| Component | Detail |
|-----------|--------|
| Port | 8085 |
| Auth | JWT validation (shared secret with Auth Service 8081) |
| DB | Shared PostgreSQL with Auth (8081), Task (8082), User (8083), Payment (8084) |
| Tables | ratings, flags, user_rating_summaries |
| Indexes | B-tree on (given_to, created_at), (status, created_at), weighted_rating DESC |

## Testing

```bash
mvn test    # Runs WeightedRatingCalculatorTest (12 test cases)
```

Tests cover: simple average, weighted decay, recent bias, same-day equality, empty/null inputs, star distribution, very old ratings, range clamping.

## Integration with Other Services

- **Task Service (8082)**: Rating Service validates task_id exists (future: verify task status is PAYMENT_DONE)
- **User Profile Service (8083)**: `weighted_rating` from summaries feeds into worker profile `average_rating` and `total_ratings` fields
- **Payment Service (8084)**: Ratings are only allowed after task reaches PAYMENT_DONE status

## License

Proprietary - Grace and Faith Research and Development Private Limited
