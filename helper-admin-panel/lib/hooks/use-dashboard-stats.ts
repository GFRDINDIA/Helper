import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type {
  UserStats,
  TaskStats,
  PaymentStats,
  RatingStats,
  KycStats,
  NotificationStats,
  ApiResponse,
} from "@/lib/types";

const STALE_TIME = 30_000; // 30 seconds

export function useUserStats() {
  return useQuery<UserStats>({
    queryKey: ["stats", "users"],
    queryFn: async () => {
      const res = await api.get<ApiResponse<UserStats>>("/api/v1/admin/stats");
      return res.data.data;
    },
    staleTime: STALE_TIME,
    retry: 1,
  });
}

export function useTaskStats() {
  return useQuery<TaskStats>({
    queryKey: ["stats", "tasks"],
    queryFn: async () => {
      const res = await api.get<ApiResponse<TaskStats>>("/api/v1/tasks/admin/stats");
      return res.data.data;
    },
    staleTime: STALE_TIME,
    retry: 1,
  });
}

export function usePaymentStats() {
  return useQuery<PaymentStats>({
    queryKey: ["stats", "payments"],
    queryFn: async () => {
      const res = await api.get<ApiResponse<PaymentStats>>("/api/v1/admin/payments/stats");
      return res.data.data;
    },
    staleTime: STALE_TIME,
    retry: 1,
  });
}

export function useRatingStats() {
  return useQuery<RatingStats>({
    queryKey: ["stats", "ratings"],
    queryFn: async () => {
      const res = await api.get<ApiResponse<RatingStats>>("/api/v1/admin/ratings/stats");
      return res.data.data;
    },
    staleTime: STALE_TIME,
    retry: 1,
  });
}

export function useKycStats() {
  return useQuery<KycStats>({
    queryKey: ["stats", "kyc"],
    queryFn: async () => {
      const res = await api.get<ApiResponse<KycStats>>("/api/v1/admin/kyc/stats");
      return res.data.data;
    },
    staleTime: STALE_TIME,
    retry: 1,
  });
}

export function useNotificationStats() {
  return useQuery<NotificationStats>({
    queryKey: ["stats", "notifications"],
    queryFn: async () => {
      const res = await api.get<ApiResponse<NotificationStats>>("/api/v1/admin/notifications/stats");
      return res.data.data;
    },
    staleTime: STALE_TIME,
    retry: 1,
  });
}
