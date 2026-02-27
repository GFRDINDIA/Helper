// Base URL — 10.0.2.2 maps to host machine's localhost from Android emulator
// For physical device on same WiFi, replace with your machine's LAN IP (e.g. 192.168.1.x:8080)
const String kBaseUrl = 'http://10.0.2.2:8080';

class ApiEndpoints {
  // Auth
  static const String register = '/api/v1/auth/register';
  static const String login = '/api/v1/auth/login';
  static const String verifyEmail = '/api/v1/auth/verify-email';
  static const String resendOtp = '/api/v1/auth/resend-otp';
  static const String refresh = '/api/v1/auth/refresh';
  static const String logout = '/api/v1/auth/logout';

  // Customer
  static const String customerMe = '/api/v1/customers/me';
  static const String customerProfile = '/api/v1/customers/profile';

  // Worker
  static const String workerMe = '/api/v1/workers/me';
  static const String workerProfile = '/api/v1/workers/profile';
  static const String workersNearby = '/api/v1/workers/nearby';

  // Tasks
  static const String tasks = '/api/v1/tasks';
  static const String tasksAvailable = '/api/v1/tasks/available';

  static String taskById(String id) => '/api/v1/tasks/$id';
  static String taskStatus(String id) => '/api/v1/tasks/$id/status';

  // Bids
  static const String bids = '/api/v1/bids';
  static String bidsByTask(String taskId) => '/api/v1/bids/task/$taskId';
  static String bidsByWorker(String workerId) => '/api/v1/bids/worker/$workerId';
  static String acceptBid(String bidId) => '/api/v1/bids/$bidId/accept';
  static String withdrawBid(String bidId) => '/api/v1/bids/$bidId/withdraw';

  // Payments
  static const String paymentsInitiate = '/api/v1/payments/initiate';
  static String paymentPay(String id) => '/api/v1/payments/$id/pay';
  static String paymentConfirmCash(String id) => '/api/v1/payments/$id/confirm-cash';
  static String paymentTip(String id) => '/api/v1/payments/$id/tip';
  static const String paymentHistory = '/api/v1/payments/history';

  // Ratings
  static const String ratings = '/api/v1/ratings';
  static String ratingsByWorker(String workerId) => '/api/v1/ratings/worker/$workerId';
  static String ratingsByCustomer(String customerId) =>
      '/api/v1/ratings/customer/$customerId';

  // KYC
  static const String kycUpload = '/api/v1/kyc/upload';
  static const String kycStatus = '/api/v1/kyc/status';
}
