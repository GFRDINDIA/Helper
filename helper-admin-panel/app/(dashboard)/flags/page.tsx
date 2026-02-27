"use client";

import { useState } from "react";
import { useFlags, useReviewFlag } from "@/lib/hooks/use-flags";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import { Flag, Star, AlertCircle } from "lucide-react";
import type { RatingFlag } from "@/lib/types";

const STATUS_OPTIONS = [
  { value: "PENDING", label: "Pending" },
  { value: "DISMISSED", label: "Dismissed" },
  { value: "ACTION_TAKEN", label: "Action Taken" },
];

function statusBadge(status: string) {
  const map: Record<string, string> = {
    PENDING: "bg-yellow-100 text-yellow-800",
    DISMISSED: "bg-gray-100 text-gray-800",
    ACTION_TAKEN: "bg-red-100 text-red-800",
  };
  return (
    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${map[status] ?? "bg-gray-100 text-gray-800"}`}>
      {status.replace("_", " ")}
    </span>
  );
}

export default function FlagsPage() {
  const [statusFilter, setStatusFilter] = useState("PENDING");
  const [selected, setSelected] = useState<RatingFlag | null>(null);
  const [reviewAction, setReviewAction] = useState<"DISMISS" | "ACTION_TAKEN">("DISMISS");
  const [hideRating, setHideRating] = useState(false);
  const [adminNote, setAdminNote] = useState("");

  const { data: flags = [], isLoading, isError } = useFlags(statusFilter);
  const reviewFlag = useReviewFlag();

  const openDialog = (flag: RatingFlag, action: "DISMISS" | "ACTION_TAKEN") => {
    setSelected(flag);
    setReviewAction(action);
    setHideRating(false);
    setAdminNote("");
  };

  const handleReview = () => {
    if (!selected) return;
    reviewFlag.mutate(
      {
        flagId: selected.id,
        payload: {
          action: reviewAction,
          hideRating: reviewAction === "ACTION_TAKEN" ? hideRating : false,
          adminNote: adminNote || undefined,
        },
      },
      { onSuccess: () => setSelected(null) }
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <div className="flex items-center gap-2">
          <Flag className="h-6 w-6 text-muted-foreground" />
          <h1 className="text-2xl font-bold tracking-tight">Flags & Disputes</h1>
        </div>
        <p className="text-muted-foreground mt-1">
          Review reported ratings and resolve disputes.
        </p>
      </div>

      {/* Filter */}
      <div className="flex items-center gap-3">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {STATUS_OPTIONS.map((o) => (
              <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        {!isLoading && (
          <span className="text-sm text-muted-foreground ml-auto">
            {flags.length} flag{flags.length !== 1 ? "s" : ""}
          </span>
        )}
      </div>

      {/* Table */}
      <div className="rounded-lg border bg-card">
        {isError ? (
          <div className="flex items-center gap-2 p-8 text-sm text-destructive justify-center">
            <AlertCircle className="h-4 w-4" />
            Failed to load flags.
          </div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Flagged By</TableHead>
                <TableHead>Target User</TableHead>
                <TableHead>Rating</TableHead>
                <TableHead>Reason</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Date</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading
                ? Array.from({ length: 6 }).map((_, i) => (
                    <TableRow key={i}>
                      {Array.from({ length: 7 }).map((_, j) => (
                        <TableCell key={j}><Skeleton className="h-4 w-full" /></TableCell>
                      ))}
                    </TableRow>
                  ))
                : flags.length === 0
                ? (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-12 text-muted-foreground">
                        No {statusFilter.toLowerCase().replace("_", " ")} flags.
                      </TableCell>
                    </TableRow>
                  )
                : flags.map((flag) => (
                    <TableRow key={flag.id}>
                      <TableCell className="font-medium">{flag.flaggedByName}</TableCell>
                      <TableCell className="text-muted-foreground">{flag.targetUserName}</TableCell>
                      <TableCell>
                        <span className="inline-flex items-center gap-1 text-sm">
                          <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
                          {flag.ratingScore}
                        </span>
                      </TableCell>
                      <TableCell className="max-w-[200px] truncate text-sm" title={flag.reason}>
                        {flag.reason}
                      </TableCell>
                      <TableCell>{statusBadge(flag.status)}</TableCell>
                      <TableCell className="text-muted-foreground text-xs">
                        {new Date(flag.createdAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell className="text-right">
                        {flag.status === "PENDING" && (
                          <div className="flex items-center gap-2 justify-end">
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => openDialog(flag, "DISMISS")}
                            >
                              Dismiss
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              className="text-destructive hover:text-destructive border-red-200 hover:bg-red-50"
                              onClick={() => openDialog(flag, "ACTION_TAKEN")}
                            >
                              Take Action
                            </Button>
                          </div>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
            </TableBody>
          </Table>
        )}
      </div>

      {/* Review Dialog */}
      <Dialog open={!!selected} onOpenChange={() => setSelected(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {reviewAction === "DISMISS" ? "Dismiss Flag" : "Take Action on Flag"}
            </DialogTitle>
            <DialogDescription>
              {reviewAction === "DISMISS"
                ? "Dismiss this flag — the rating will remain visible."
                : "Take action against this flag. You can optionally hide the offending rating."}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-3 py-2">
            {/* Flag detail summary */}
            <div className="rounded-md bg-muted/50 p-3 space-y-1 text-sm">
              <p>
                <span className="text-muted-foreground">Flagged by: </span>
                <span className="font-medium">{selected?.flaggedByName}</span>
              </p>
              <p>
                <span className="text-muted-foreground">Against: </span>
                <span className="font-medium">{selected?.targetUserName}</span>
              </p>
              <p>
                <span className="text-muted-foreground">Reason: </span>
                {selected?.reason}
              </p>
              {selected?.ratingComment && (
                <p>
                  <span className="text-muted-foreground">Review text: </span>
                  <em>{selected.ratingComment}</em>
                </p>
              )}
            </div>

            {reviewAction === "ACTION_TAKEN" && (
              <label className="flex items-center gap-2 text-sm cursor-pointer">
                <input
                  type="checkbox"
                  checked={hideRating}
                  onChange={(e) => setHideRating(e.target.checked)}
                  className="rounded"
                />
                Hide this rating from the platform
              </label>
            )}

            <div className="space-y-1.5">
              <label className="text-sm font-medium">Admin Note (optional)</label>
              <Textarea
                placeholder="Internal note about this decision..."
                value={adminNote}
                onChange={(e) => setAdminNote(e.target.value)}
                rows={3}
              />
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setSelected(null)}>
              Cancel
            </Button>
            <Button
              variant={reviewAction === "ACTION_TAKEN" ? "destructive" : "default"}
              onClick={handleReview}
              disabled={reviewFlag.isPending}
            >
              {reviewFlag.isPending
                ? "Processing..."
                : reviewAction === "DISMISS"
                ? "Dismiss Flag"
                : "Take Action"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
