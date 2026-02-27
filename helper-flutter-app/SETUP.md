# Helper Flutter App — Setup Guide

## Prerequisites
- Flutter SDK (stable channel) — https://docs.flutter.dev/get-started/install/windows/mobile
- Android Studio + Android SDK (API 21+)
- Android Emulator OR physical Android device

## 1. Install Flutter (if not already installed)
```powershell
# Download Flutter SDK and add to PATH
# Follow: https://docs.flutter.dev/get-started/install/windows/mobile
```

## 2. Install dependencies
```bash
cd helper-flutter-app
flutter pub get
```

## 3. Run code generation (for riverpod_generator)
```bash
dart run build_runner build --delete-conflicting-outputs
```

## 4. Run the app
```bash
# Start an Android emulator first in Android Studio
flutter run
```

## API Connection
- **Android Emulator**: API base URL is `http://10.0.2.2:8080` (maps to your localhost:8080)
- **Physical Device**: Change `kBaseUrl` in `lib/core/constants/api_constants.dart` to your machine's LAN IP:
  ```dart
  const String kBaseUrl = 'http://192.168.1.X:8080';  // your LAN IP
  ```

## Test Accounts (from backend seed data)
| Role     | Email              | Password  |
|----------|--------------------|-----------|
| CUSTOMER | customer@test.com  | Test@123  |
| WORKER   | worker@test.com    | Test@123  |

## End-to-End Test Flow
1. Register as CUSTOMER → verify OTP (check backend logs for OTP) → login
2. Post a BIDDING task (Plumbing, your location)
3. Login as WORKER → browse available tasks → submit bid
4. Switch back to CUSTOMER → view task → accept bid
5. WORKER marks task IN_PROGRESS → then COMPLETED
6. CUSTOMER goes to task → Pay Now → select CASH
7. Both users rate each other

## Project Structure
```
lib/
├── core/            # Infrastructure: API, storage, router
├── features/
│   ├── auth/        # Login, register, OTP
│   ├── customer/    # Task posting, bids, nearby workers
│   ├── worker/      # Browse tasks, bid submission
│   ├── profile/     # Profile edit, KYC upload
│   ├── payment/     # Payment flow, history
│   └── rating/      # Star rating screen
└── shared/          # Theme, widgets
```
