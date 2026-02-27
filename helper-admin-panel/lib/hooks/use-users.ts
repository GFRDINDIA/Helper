import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import type { ApiResponse, PagedResponse, PlatformUser } from "@/lib/types";

interface UserFilters {
  role?: string;
  verificationStatus?: string;
  page?: number;
  size?: number;
}

export function useUsers(filters: UserFilters = {}) {
  const { role, verificationStatus, page = 0, size = 20 } = filters;

  return useQuery<PagedResponse<PlatformUser>>({
    queryKey: ["users", role, verificationStatus, page, size],
    queryFn: async () => {
      const params: Record<string, string | number> = { page, size };
      if (role) params.role = role;
      if (verificationStatus) params.verificationStatus = verificationStatus;

      const res = await api.get<ApiResponse<PagedResponse<PlatformUser>>>(
        "/api/v1/admin/users",
        { params }
      );
      return res.data.data;
    },
    staleTime: 30_000,
    retry: 1,
  });
}

export function useActivateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (userId: string) =>
      api.put(`/api/v1/admin/users/${userId}/activate`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["users"] }),
  });
}

export function useDeactivateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (userId: string) =>
      api.put(`/api/v1/admin/users/${userId}/deactivate`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["users"] }),
  });
}
