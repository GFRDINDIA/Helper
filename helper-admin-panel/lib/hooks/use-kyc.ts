import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import type { ApiResponse, KycDocument, KycReviewRequest } from "@/lib/types";

export function useKycQueue(status: string = "PENDING") {
  return useQuery<KycDocument[]>({
    queryKey: ["kyc", status],
    queryFn: async () => {
      const res = await api.get<ApiResponse<KycDocument[]>>(
        "/api/v1/admin/kyc/pending",
        { params: { status } }
      );
      return res.data.data ?? [];
    },
    staleTime: 30_000,
    retry: 1,
  });
}

export function useReviewKyc() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      documentId,
      payload,
    }: {
      documentId: string;
      payload: KycReviewRequest;
    }) => api.put(`/api/v1/admin/kyc/${documentId}/review`, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["kyc"] }),
  });
}
