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

// ===== PAGINATION =====
export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

// ===== USERS =====
export interface PlatformUser {
  userId: string;
  fullName: string;
  email: string;
  phone: string;
  role: "ADMIN" | "CUSTOMER" | "WORKER";
  verificationStatus: "PENDING" | "VERIFIED" | "REJECTED" | "SUSPENDED";
  emailVerified: boolean;
  profileImageUrl: string | null;
  createdAt: string;
}

// ===== KYC =====
export interface KycDocument {
  id: string;
  userId: string;
  userName: string;
  email: string;
  documentType: string;
  documentUrl: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  submittedAt: string;
  reviewedAt: string | null;
  adminNotes: string | null;
}

export interface KycReviewRequest {
  status: "APPROVED" | "REJECTED";
  adminNotes?: string;
}

// ===== FLAGS =====
export interface RatingFlag {
  id: string;
  ratingId: string;
  flaggedByUserId: string;
  flaggedByName: string;
  reason: string;
  status: "PENDING" | "DISMISSED" | "ACTION_TAKEN";
  ratingScore: number;
  ratingComment: string;
  taskId: string;
  targetUserId: string;
  targetUserName: string;
  createdAt: string;
}

export interface FlagReviewRequest {
  action: "DISMISS" | "ACTION_TAKEN";
  hideRating?: boolean;
  adminNote?: string;
}

// ===== API WRAPPER =====
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}
