import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import type { ApiResponse, RatingFlag, FlagReviewRequest } from "@/lib/types";

export function useFlags(status: string = "PENDING") {
  return useQuery<RatingFlag[]>({
    queryKey: ["flags", status],
    queryFn: async () => {
      const res = await api.get<ApiResponse<RatingFlag[]>>(
        "/api/v1/admin/flags/pending",
        { params: { status } }
      );
      return res.data.data ?? [];
    },
    staleTime: 30_000,
    retry: 1,
  });
}

export function useReviewFlag() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      flagId,
      payload,
    }: {
      flagId: string;
      payload: FlagReviewRequest;
    }) => api.put(`/api/v1/admin/flags/${flagId}/review`, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["flags"] }),
  });
}
