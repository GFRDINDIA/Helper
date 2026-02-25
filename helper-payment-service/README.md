# Helper Payment Service

> Payment processing, 2% commission calculation, 18% GST, worker ledger, tip handling, and PDF invoice generation for the **Helper** marketplace platform.

## Quick Start

```bash
cd helper-payment-service
mvn spring-boot:run    # Starts on port 8084 with H2
```

- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **H2 Console**: http://localhost:8084/h2-console

## Commission Formula (PRD Section 3.5)

```
commission     = finalPrice × 2%           (configurable via admin)
tax (GST)      = commission × 18%           (configurable via admin)
workerPayout   = finalPrice - commission - tax + tip
customerTotal  = finalPrice + tip
```

**Example: ₹1,000 task + ₹100 tip**

| Line Item | Formula | Amount |
|-----------|---------|--------|
| Task Price | - | ₹1,000.00 |
| Commission (2%) | 1000 × 0.02 | ₹20.00 |
| GST on Commission (18%) | 20 × 0.18 | ₹3.60 |
| **Worker Receives** | 1000 - 20 - 3.60 + 100 | **₹1,076.40** |
| **Customer Pays** | 1000 + 100 | **₹1,100.00** |
| **Platform Revenue** | commission | **₹20.00** |

**Key Rules:**
- Commission charged to WORKER (deducted from payout), NOT added to customer bill
- Tips go 100% to worker — ZERO commission on tips
- All calculations use BigDecimal with HALF_UP rounding (paisa precision)
- Rates are admin-configurable via the platform_config table

## API Endpoints

### Payment Endpoints (17 total)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/payments/initiate` | CUSTOMER | Initiate payment for completed task |
| PUT | `/api/v1/payments/{id}/confirm` | WORKER | Confirm cash received |
| POST | `/api/v1/payments/{id}/tip` | CUSTOMER | Add tip (100% to worker) |
| GET | `/api/v1/payments/{id}` | AUTH | Get payment details |
| GET | `/api/v1/payments/task/{taskId}` | AUTH | Get payment by task |
| GET | `/api/v1/payments/my-transactions` | AUTH | Transaction history (paginated) |
| GET | `/api/v1/payments/invoices/{id}` | AUTH | Download invoice |
| POST | `/api/v1/payments/callback` | - | [Phase 2] Razorpay webhook |

### Worker Ledger

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/workers/ledger` | WORKER | Commission owed/paid history |
| GET | `/api/v1/workers/ledger/balance` | WORKER | Current outstanding balance |

### Admin

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/admin/payments/stats` | ADMIN | Revenue, commission, tax summary |
| GET | `/api/v1/admin/payments/transactions` | ADMIN | All transactions with filters |
| POST | `/api/v1/admin/payments/{id}/refund` | ADMIN | Refund a payment |
| GET | `/api/v1/admin/config` | ADMIN | Get all config values |
| PUT | `/api/v1/admin/config/{key}` | ADMIN | Update commission rate, GST, etc. |

## Database Schema

### payments (PRD Section 4.1)
Core transaction table: payment_id, task_id (unique), payer_id, payee_id, amount, commission, commission_rate, tax, tax_rate, tip, worker_payout, method, status, invoice_number, invoice_url, payment_reference, processed_at.

### worker_ledger (Cash commission tracking)
Tracks commission owed by workers from cash payments: worker_id, payment_id, type (COMMISSION_DUE/PAID/BONUS/PENALTY), amount, balance_after (running balance).

### platform_config (Admin-configurable)
Key-value store: COMMISSION_RATE (default 0.02), GST_RATE (default 0.18), CANCELLATION_FEE_RATE (default 0.10).

## Cash Payment Flow (MVP)

1. Task reaches COMPLETED status
2. Customer taps "Pay" → `POST /api/v1/payments/initiate` (method=CASH)
3. System calculates commission (2%) + GST (18% on commission)
4. Payment created as PENDING + ledger entry for commission owed
5. Invoice PDF auto-generated
6. Worker confirms cash → `PUT /api/v1/payments/{id}/confirm`
7. Payment status → COMPLETED
8. Task status → PAYMENT_DONE (via Task Service)

## Invoice Generation

Each payment generates a PDF invoice with:
- Invoice number (HLP-INV-2026-XXXXXX)
- Customer and worker details
- Full price breakdown (amount, commission, GST, tip)
- Company GSTIN
- Stored locally (dev) or S3 (production)

## Sample Dev Data

On startup creates:
- **Payment 1**: ₹500 plumbing task, commission ₹10 + ₹1.80 GST, payout ₹488.20
- **Payment 2**: ₹800 electrician task + ₹100 tip, payout ₹881.12
- **Payment 3**: ₹200 delivery task (pending confirmation)
- **Worker 1 ledger**: Owes ₹30.68 in commissions
- **Worker 2 ledger**: Owes ₹4.72 in commissions

## Architecture

- **Port**: 8084
- **Auth**: JWT validation (shared secret with Auth Service on 8081)
- **DB**: Shared PostgreSQL with Auth (8081), Task (8082), User (8083)
- **Tables**: payments, worker_ledger, platform_config
- **Invoice PDF**: openhtmltopdf (HTML → PDF)

## Testing

```bash
mvn test    # Runs PaymentCalculatorTest (14 test cases)
```

PaymentCalculator tests cover: PRD example, zero tip, null tip, zero commission, high commission, small amounts, large amounts, rounding precision, negative values (should throw), cancellation fee.

## Phase 2 Preparation

- `payment_reference` field ready for Razorpay payment_id
- `PaymentMethod` enum includes UPI, CARD, NET_BANKING, WALLET
- `/callback` endpoint placeholder for Razorpay webhooks
- Worker ledger designed for auto-deduction from digital payouts

## License

Proprietary - Grace and Faith Research and Development Private Limited
