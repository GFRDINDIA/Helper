# Helper Auth Service

> Authentication & Authorization microservice for the **Helper** marketplace platform.  
> Built with Spring Boot 3.2 + PostgreSQL + JWT + Email OTP verification.

---

## Project Structure

```
helper-auth-service/
├── pom.xml                          # Maven dependencies & build config
├── Dockerfile                       # Multi-stage Docker build
├── docker-compose.yml               # Local dev with PostgreSQL + Redis
├── init-db.sql                      # Database initialization script
├── .env.example                     # Environment variable template
├── .gitignore
├── README.md                        # This file
│
└── src/
    └── main/
        ├── java/com/helper/auth/
        │   ├── HelperAuthServiceApplication.java    # Main entry point
        │   │
        │   ├── config/
        │   │   ├── SecurityConfig.java              # Spring Security + CORS + JWT filter chain
        │   │   ├── OpenApiConfig.java               # Swagger/OpenAPI documentation
        │   │   └── DataInitializer.java             # Seeds default admin (dev profile only)
        │   │
        │   ├── controller/
        │   │   ├── AuthController.java              # Public auth endpoints (register, login, OTP, etc.)
        │   │   ├── AdminController.java             # Admin-only endpoints (user mgmt, KYC approval)
        │   │   └── HealthController.java            # Health check endpoint
        │   │
        │   ├── dto/
        │   │   ├── request/
        │   │   │   ├── RegisterRequest.java         # Registration payload
        │   │   │   ├── LoginRequest.java            # Login payload
        │   │   │   ├── VerifyOtpRequest.java        # OTP verification payload
        │   │   │   ├── ResendOtpRequest.java        # Resend OTP payload
        │   │   │   ├── RefreshTokenRequest.java     # Token refresh payload
        │   │   │   ├── ForgotPasswordRequest.java   # Forgot password payload
        │   │   │   ├── ResetPasswordRequest.java    # Reset password with OTP
        │   │   │   └── ChangePasswordRequest.java   # Change password (authenticated)
        │   │   └── response/
        │   │       ├── AuthResponse.java            # Auth response with tokens + user info
        │   │       └── ApiResponse.java             # Standard API response wrapper
        │   │
        │   ├── entity/
        │   │   ├── User.java                        # Users table entity
        │   │   ├── RefreshToken.java                # Refresh tokens entity
        │   │   └── OtpToken.java                    # OTP tokens entity
        │   │
        │   ├── enums/
        │   │   ├── Role.java                        # CUSTOMER, WORKER, ADMIN
        │   │   └── VerificationStatus.java          # PENDING, VERIFIED, REJECTED
        │   │
        │   ├── exception/
        │   │   ├── AuthExceptions.java              # Custom exception classes
        │   │   └── GlobalExceptionHandler.java      # Centralized error handling
        │   │
        │   ├── repository/
        │   │   ├── UserRepository.java
        │   │   ├── RefreshTokenRepository.java
        │   │   └── OtpTokenRepository.java
        │   │
        │   ├── security/
        │   │   ├── JwtTokenProvider.java            # JWT generation & validation
        │   │   ├── JwtAuthenticationFilter.java     # Extract JWT from requests
        │   │   ├── JwtAuthenticationEntryPoint.java # Handle 401 responses
        │   │   ├── CustomUserDetails.java           # Spring Security UserDetails impl
        │   │   └── CustomUserDetailsService.java    # Load user from database
        │   │
        │   └── service/
        │       ├── AuthService.java                 # Core auth business logic
        │       └── OtpService.java                  # OTP generation, email sending, verification
        │
        └── resources/
            ├── application.properties               # Main config (shared)
            ├── application-dev.properties            # Dev profile (H2 in-memory DB)
            ├── application-prod.properties           # Prod profile (PostgreSQL + Redis)
            └── db/migration/
                └── V1__Create_Auth_Tables.sql        # Flyway migration script
```

---

## Quick Start

### Option 1: Run with H2 (Fastest - No External Dependencies)

```bash
# Clone and navigate to the project
cd helper-auth-service

# Build and run (uses H2 in-memory database)
./mvnw spring-boot:run

# OR with Maven installed
mvn spring-boot:run
```

The service starts at `http://localhost:8081` with:
- **H2 Console**: http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:helperdb`)
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs**: http://localhost:8081/api-docs

**Default test accounts (dev profile):**

| Role     | Email              | Password  |
|----------|--------------------|-----------|
| Admin    | admin@helper.app   | Admin@123 |
| Customer | customer@test.com  | Test@123  |
| Worker   | worker@test.com    | Test@123  |

### Option 2: Run with Docker Compose (PostgreSQL + Redis)

```bash
# Copy and configure environment
cp .env.example .env
# Edit .env with your values

# Start everything
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop
docker-compose down
```

---

## API Endpoints

### Public Endpoints (No Authentication Required)

| Method | Endpoint                        | Description                              |
|--------|---------------------------------|------------------------------------------|
| POST   | `/api/v1/auth/register`         | Register new CUSTOMER or WORKER          |
| POST   | `/api/v1/auth/login`            | Login with email + password              |
| POST   | `/api/v1/auth/verify-otp`       | Verify email OTP (registration/reset)    |
| POST   | `/api/v1/auth/resend-otp`       | Resend OTP (rate limited: 1/60s)         |
| POST   | `/api/v1/auth/refresh`          | Refresh access token                     |
| POST   | `/api/v1/auth/forgot-password`  | Request password reset OTP               |
| POST   | `/api/v1/auth/reset-password`   | Reset password with OTP                  |

### Authenticated Endpoints (Bearer Token Required)

| Method | Endpoint                        | Description                              |
|--------|---------------------------------|------------------------------------------|
| GET    | `/api/v1/auth/me`               | Get current user profile                 |
| POST   | `/api/v1/auth/change-password`  | Change password (requires current)       |
| POST   | `/api/v1/auth/logout`           | Logout current session                   |
| POST   | `/api/v1/auth/logout-all`       | Logout from all devices                  |

### Admin Endpoints (ADMIN Role Required)

| Method | Endpoint                              | Description                        |
|--------|---------------------------------------|------------------------------------|
| GET    | `/api/v1/admin/users`                 | List all users (filter by role)    |
| PUT    | `/api/v1/admin/users/{id}/verify`     | Approve/Reject worker KYC          |
| PUT    | `/api/v1/admin/users/{id}/deactivate` | Deactivate user account            |
| PUT    | `/api/v1/admin/users/{id}/activate`   | Reactivate user account            |
| GET    | `/api/v1/admin/stats`                 | Platform user statistics           |

---

## API Usage Examples

### 1. Register a New Customer

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Rahul Sharma",
    "email": "rahul@example.com",
    "phone": "+919876543210",
    "password": "MyPass@123",
    "role": "CUSTOMER"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful. Please check your email for the verification code.",
  "timestamp": "2026-02-17T10:30:00"
}
```

### 2. Verify Email OTP

```bash
curl -X POST http://localhost:8081/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "rahul@example.com",
    "otpCode": "123456",
    "purpose": "REGISTRATION"
  }'
```

**Response (auto-login after verification):**
```json
{
  "success": true,
  "message": "Verification successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "a1b2c3d4e5f6...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "Rahul Sharma",
      "email": "rahul@example.com",
      "role": "CUSTOMER",
      "verificationStatus": "PENDING",
      "emailVerified": true
    }
  }
}
```

### 3. Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "rahul@example.com",
    "password": "MyPass@123",
    "deviceInfo": "Android 14 / Flutter App"
  }'
```

### 4. Access Protected Endpoint

```bash
curl -X GET http://localhost:8081/api/v1/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 5. Refresh Token

```bash
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "a1b2c3d4e5f6..."
  }'
```

---

## Authentication Flow

```
Registration:
  Register → Email OTP Sent → Verify OTP → Auto-Login (tokens returned)

Login:
  Login → Validate Credentials → Check Email Verified → Return Tokens

Token Refresh:
  Send Refresh Token → Validate → Rotate (old revoked) → New Tokens

Password Reset:
  Forgot Password → OTP Sent → Verify OTP + New Password → All Sessions Revoked
```

---

## Security Features

- **BCrypt hashing** (cost factor 12) for passwords
- **JWT access tokens** (15 min expiry) + **refresh token rotation** (7 day expiry)
- **OTP rate limiting**: 1 OTP per 60 seconds, max 5 verification attempts
- **Email enumeration prevention**: Forgot password always returns success
- **CORS configuration**: Restricted to known origins
- **Role-based access**: CUSTOMER, WORKER, ADMIN with method-level security
- **Automatic token cleanup**: Scheduled jobs remove expired tokens
- **Input validation**: Bean validation on all request DTOs

---

## Configuration

All configurable values are in `application.properties`. Key settings:

| Property | Default | Description |
|----------|---------|-------------|
| `app.jwt.access-token-expiration-ms` | 900000 (15 min) | Access token lifetime |
| `app.jwt.refresh-token-expiration-ms` | 604800000 (7 days) | Refresh token lifetime |
| `app.otp.length` | 6 | OTP digit count |
| `app.otp.expiration-minutes` | 10 | OTP validity period |
| `app.otp.max-attempts` | 5 | Max OTP verification attempts |
| `app.otp.resend-cooldown-seconds` | 60 | Cooldown between OTP resends |
| `app.platform.commission-percent` | 2.0 | Platform commission (for reference) |

---

## Email Setup (Gmail)

1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password: Google Account → Security → App passwords
3. Set in `.env`:
   ```
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-16-char-app-password
   ```

**Note**: In dev mode, if email sending fails, the OTP is logged to console for testing.

---

## Next Steps (Other Microservices)

This auth service is **Phase 1** of the Helper platform. Next services to build:

1. **User Profile Service** - Extended profiles, KYC document upload
2. **Task Service** - Task CRUD, lifecycle management, geo-search
3. **Bidding Service** - Open bidding + fixed price acceptance
4. **Payment Service** - Razorpay integration, commission, invoicing
5. **Notification Service** - Push notifications, SMS, email
6. **Geo Service** - PostGIS integration, worker matching
7. **API Gateway** - Spring Cloud Gateway for routing

---

## License

Proprietary - Grace and Faith Research and Development Private Limited
