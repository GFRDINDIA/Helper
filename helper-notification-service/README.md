# Helper Notification Service

> Push (Firebase), SMS (MSG91), Email (SMTP), and In-App notifications for all **Helper** marketplace lifecycle events.

## Quick Start

```bash
cd helper-notification-service
mvn spring-boot:run    # Starts on port 8086 with H2
```

- **Swagger UI**: http://localhost:8086/swagger-ui.html
- **H2 Console**: http://localhost:8086/h2-console

## PRD Event → Channel Matrix (Section 3.8)

| Event | Push | In-App | SMS | Email |
|-------|:----:|:------:|:---:|:-----:|
| New task in area | ✅ | ✅ | | |
| New bid received | ✅ | ✅ | | |
| Bid accepted/rejected | ✅ | ✅ | ✅ | |
| Task status change | ✅ | ✅ | | |
| Task cancelled | ✅ | ✅ | ✅ | |
| Payment received | ✅ | ✅ | ✅ | |
| Payment refunded | ✅ | ✅ | | |
| Rating received | | ✅ | | |
| KYC submitted | | ✅ | | |
| KYC approved/rejected | ✅ | ✅ | ✅ | ✅ |
| Promotional | ✅ | | | ✅ |
| System alert | ✅ | ✅ | | ✅ |

This matrix is implemented in `NotificationEventRouter.java` — the single source of truth.

## API Endpoints (20 total)

### User — Notification Inbox

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/notifications` | AUTH | My notifications (paginated) |
| GET | `/api/v1/notifications/unread` | AUTH | Unread only |
| GET | `/api/v1/notifications/unread/count` | AUTH | Badge count |
| PUT | `/api/v1/notifications/{id}/read` | AUTH | Mark as read |
| PUT | `/api/v1/notifications/read-all` | AUTH | Mark all read |
| DELETE | `/api/v1/notifications/{id}` | AUTH | Delete notification |

### Devices & Preferences

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/devices/register` | AUTH | Register FCM token |
| DELETE | `/api/v1/devices/deactivate` | AUTH | Deactivate on logout |
| GET | `/api/v1/devices/my-devices` | AUTH | List registered devices |
| GET | `/api/v1/devices/preferences` | AUTH | Get notification preferences |
| PUT | `/api/v1/devices/preferences` | AUTH | Update preferences |

### Internal — Service-to-Service

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/internal/notify/task-status` | AUTH | Task status change |
| POST | `/api/v1/internal/notify/payment-received` | AUTH | Payment to worker |
| POST | `/api/v1/internal/notify/new-bid` | AUTH | New bid on task |
| POST | `/api/v1/internal/notify/bid-accepted` | AUTH | Bid accepted |
| POST | `/api/v1/internal/notify/rating-received` | AUTH | New rating |
| POST | `/api/v1/internal/notify/kyc-status` | AUTH | KYC approved/rejected |

### Admin

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/admin/notifications/send` | ADMIN | Send to specific users |
| GET | `/api/v1/admin/notifications/stats` | ADMIN | Notification statistics |

## Architecture

```
Other Services → InternalNotificationController
                         ↓
               NotificationService
                         ↓
              NotificationEventRouter (PRD matrix)
                    ↙    ↓    ↘
             Push   SMS   Email   In-App
          (Firebase) (MSG91) (SMTP) (DB stored)
```

### Channel Dispatchers

Each channel has its own dispatcher class using the Strategy pattern:

- **PushNotificationDispatcher**: Firebase Cloud Messaging. Stores FCM tokens per user. Auto-deactivates stale tokens.
- **SmsDispatcher**: MSG91 for India, Twilio for global. Template-based SMS.
- **EmailDispatcher**: Spring Mail + Thymeleaf HTML templates. Responsive email design.
- **In-App**: Stored in `notifications` table. Retrieved via REST API. Supports read/unread/delete.

### User Preferences

Users can toggle per-channel:
- Push on/off
- SMS on/off
- Email on/off
- In-App on/off
- Quiet hours (e.g. 10PM–7AM: only in-app, no push/SMS)
- Promotional opt-out

### Reliability

- **Async dispatch**: All channel sends are `@Async` — don't block the API response.
- **Retry failed**: Scheduled job retries FAILED notifications up to 3 times.
- **Auto-cleanup**: Deletes notifications older than 90 days (3 AM daily cron).
- **Stale token handling**: Auto-deactivates expired FCM tokens.

## Database Schema

### notifications
Stores all in-app notifications: notification_id, user_id, event (enum), title, body, data_json (task/payment IDs), priority, status (PENDING/SENT/DELIVERED/READ/FAILED), is_read, channel delivery booleans (push_sent, sms_sent, email_sent), retry_count, error_message.

### device_tokens
FCM tokens per user per device: token_id, user_id, token (unique), platform (ANDROID/IOS/WEB), device_name, is_active.

### user_notification_preferences
Per-user channel toggles: user_id (PK), push/sms/email/in_app enabled booleans, quiet hours config, promotional opt-out.

## Sample Dev Data

- 7 notifications (bid accepted, payment received, rating, task update, KYC approved, 1 failed for retry)
- 3 device tokens (2 Android, 1 iOS)
- 2 user preferences (Worker 1: quiet hours 10PM-7AM; Customer 1: SMS off, promos off)

## Channel Configuration

All channels are **disabled in dev** (log-only). Enable in production via env vars:

```bash
# Firebase Push
APP_FIREBASE_ENABLED=true
# Place firebase-service-account.json in classpath

# Email
APP_MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=noreply@helper.app
MAIL_PASSWORD=your_app_password

# SMS (MSG91 for India)
APP_SMS_ENABLED=true
SMS_API_KEY=your_msg91_key
```

## Testing

```bash
mvn test    # Runs NotificationEventRouterTest (13 test cases)
```

Tests verify every PRD event maps to correct channels, all events have at least one channel, and the `requiresChannel` helper works.

## Service Integration

Other Helper microservices call the Internal API to trigger notifications:

```
Task Service (8082) → POST /api/v1/internal/notify/task-status
Payment Service (8084) → POST /api/v1/internal/notify/payment-received
Rating Service (8085) → POST /api/v1/internal/notify/rating-received
User Service (8083) → POST /api/v1/internal/notify/kyc-status
```

## License

Proprietary - Grace and Faith Research and Development Private Limited
