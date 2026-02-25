# Helper User Profile & KYC Service

> Worker profiles, customer profiles, skills management, KYC verification, portfolio, and geo-based worker discovery for the **Helper** marketplace platform.

## Quick Start

```bash
cd helper-user-service
mvn spring-boot:run    # Starts on port 8083 with H2
```

- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **H2 Console**: http://localhost:8083/h2-console

## API Endpoints

### Worker Profile APIs (PRD Section 8.5)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| PUT | `/api/v1/workers/profile` | WORKER | Create/update worker profile with bio, location, skills, availability |
| GET | `/api/v1/workers/{workerId}` | Public | View worker profile |
| GET | `/api/v1/workers/me` | WORKER | Get own profile |
| POST | `/api/v1/workers/skills` | WORKER | Add a skill domain (e.g., PLUMBING + FIXED pricing at ₹500) |
| DELETE | `/api/v1/workers/skills/{domain}` | WORKER | Remove a skill domain |
| PUT | `/api/v1/workers/availability` | WORKER | Set weekly schedule (days + hours) |
| PUT | `/api/v1/workers/availability/toggle` | WORKER | Go online/offline |
| POST | `/api/v1/workers/portfolio` | WORKER | Add portfolio photo |
| GET | `/api/v1/workers/{workerId}/portfolio` | Public | View worker's past work |
| DELETE | `/api/v1/workers/portfolio/{itemId}` | WORKER | Remove portfolio item |
| GET | `/api/v1/workers/nearby?lat=&lng=&domain=` | Public | Find verified workers nearby |

### Customer Profile APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| PUT | `/api/v1/customers/profile` | CUSTOMER | Create/update profile |
| GET | `/api/v1/customers/me` | CUSTOMER | Get own profile |
| POST | `/api/v1/customers/addresses` | CUSTOMER | Add saved address (Home, Office) |
| DELETE | `/api/v1/customers/addresses/{id}` | CUSTOMER | Remove address |

### KYC Verification APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/workers/kyc` | WORKER | Submit KYC documents |
| GET | `/api/v1/workers/kyc` | WORKER | View own KYC status |
| GET | `/api/v1/admin/kyc/pending` | ADMIN | Get pending review queue |
| PUT | `/api/v1/admin/kyc/{docId}/review` | ADMIN | Approve or reject with comments |
| GET | `/api/v1/admin/kyc/stats` | ADMIN | KYC statistics by status |

## KYC Verification System

### KYC Levels by Domain (India)

| Level | Domains | Required Documents |
|-------|---------|-------------------|
| **BASIC** | Delivery, Farming, Logistics, Household | Aadhaar + PAN + Selfie |
| **PROFESSIONAL** | Electrician, Plumbing, Construction | Aadhaar + PAN + Selfie + Professional License |
| **PROFESSIONAL_PLUS_LICENSE** | Medical, Finance, Education | Aadhaar + PAN + Selfie + Professional License + Regulatory License |

### KYC Workflow

1. Worker registers and adds skill domains
2. Worker submits KYC documents (system validates required docs for their highest domain level)
3. Admin reviews and approves/rejects each document with comments
4. When ALL documents are approved → Worker gets **VERIFIED** status + badge
5. If ANY document is rejected → Worker notified, can resubmit
6. Documents expire after 12 months → Worker prompted to re-verify

## Worker Discovery (Geo Search)

Workers are matched based on:
- PostGIS spatial indexing (production) / Haversine formula (dev)
- Only **VERIFIED** workers with `is_available = true` appear in search
- Filtered by domain and distance
- Results sorted by distance (nearest first) or rating

## Architecture

- **Port**: 8083
- **Auth**: Validates JWT tokens from Auth Service (shared secret)
- **DB**: Shared PostgreSQL with PostGIS
- **Tables**: worker_profiles, worker_skills, portfolio_items, availability_slots, kyc_documents, customer_profiles, customer_addresses, saved_payment_methods

## Sample Dev Data

On startup in dev profile, creates:
- **Worker 1** (Verified): Plumber + Electrician in Andheri, Mumbai. Rating 4.5, 35 tasks completed.
- **Worker 2** (Pending): Delivery in Colaba, Mumbai.
- **Customer 1**: 2 saved addresses (Home + Office), rating 4.8.

## License

Proprietary - Grace and Faith Research and Development Private Limited
