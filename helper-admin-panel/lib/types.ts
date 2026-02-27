// ===== AUTH =====
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: AdminUser;
  };
}

export interface AdminUser {
  userId: string;
  fullName: string;
  email: string;
  phone: string;
  role: "ADMIN" | "CUSTOMER" | "WORKER";
  verificationStatus: string;
  emailVerified: boolean;
  profileImageUrl: string | null;
}

// ===== STATS =====
export interface UserStats {
  totalUsers: number;
  totalCustomers: number;
  totalWorkers: number;
  totalAdmins: number;
}

export interface TaskStats {
  tasksByStatus: Record<string, number>;
  tasksByDomain: Record<string, number>;
  totalTasks: number;
}

export interface PaymentStats {
  totalRevenue: number;
  totalCommission: number;
  totalTax: number;
  totalTips: number;
  completedPayments: number;
  pendingPayments: number;
}

export interface RatingStats {
  totalRatings: number;
  platformAverage: number;
  hiddenCount: number;
  pendingFlags: number;
  dismissedFlags: number;
  actionTakenFlags: number;
}

export interface KycStats {
  pending: number;
  approved: number;
  rejected: number;
  total: number;
}

export interface NotificationStats {
  total: number;
  sent: number;
  pending: number;
  failed: number;
  activeDeviceTokens: number;
}

// ===== API WRAPPER =====
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}
