"use client";

import { useState } from "react";
import { useUsers, useActivateUser, useDeactivateUser } from "@/lib/hooks/use-users";
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
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Users, ChevronLeft, ChevronRight, AlertCircle } from "lucide-react";
import type { PlatformUser } from "@/lib/types";

const ROLE_OPTIONS = [
  { value: "ALL", label: "All Roles" },
  { value: "CUSTOMER", label: "Customer" },
  { value: "WORKER", label: "Worker" },
  { value: "ADMIN", label: "Admin" },
];

const STATUS_OPTIONS = [
  { value: "ALL", label: "All Statuses" },
  { value: "PENDING", label: "Pending" },
  { value: "VERIFIED", label: "Verified" },
  { value: "REJECTED", label: "Rejected" },
  { value: "SUSPENDED", label: "Suspended" },
];

function roleBadge(role: string) {
  const map: Record<string, string> = {
    ADMIN: "bg-purple-100 text-purple-800",
    CUSTOMER: "bg-blue-100 text-blue-800",
    WORKER: "bg-green-100 text-green-800",
  };
  return (
    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${map[role] ?? "bg-gray-100 text-gray-800"}`}>
      {role}
    </span>
  );
}

function statusBadge(status: string) {
  const map: Record<string, string> = {
    VERIFIED: "bg-green-100 text-green-800",
    PENDING: "bg-yellow-100 text-yellow-800",
    REJECTED: "bg-red-100 text-red-800",
    SUSPENDED: "bg-gray-100 text-gray-800",
  };
  return (
    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${map[status] ?? "bg-gray-100 text-gray-800"}`}>
      {status}
    </span>
  );
}

export default function UsersPage() {
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [page, setPage] = useState(0);
  const [confirmUser, setConfirmUser] = useState<{ user: PlatformUser; action: "activate" | "deactivate" } | null>(null);

  const { data, isLoading, isError } = useUsers({
    role: roleFilter !== "ALL" ? roleFilter : undefined,
    verificationStatus: statusFilter !== "ALL" ? statusFilter : undefined,
    page,
    size: 20,
  });

  const activate = useActivateUser();
  const deactivate = useDeactivateUser();

  const handleConfirm = () => {
    if (!confirmUser) return;
    const mutation = confirmUser.action === "activate" ? activate : deactivate;
    mutation.mutate(confirmUser.user.userId, {
      onSuccess: () => setConfirmUser(null),
    });
  };

  const users = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <div className="flex items-center gap-2">
          <Users className="h-6 w-6 text-muted-foreground" />
          <h1 className="text-2xl font-bold tracking-tight">Users</h1>
        </div>
        <p className="text-muted-foreground mt-1">
          Manage platform users — customers, workers, and admins.
        </p>
      </div>

      {/* Filters */}
      <div className="flex items-center gap-3 flex-wrap">
        <Select value={roleFilter} onValueChange={(v) => { setRoleFilter(v); setPage(0); }}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Filter by role" />
          </SelectTrigger>
          <SelectContent>
            {ROLE_OPTIONS.map((o) => (
              <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select value={statusFilter} onValueChange={(v) => { setStatusFilter(v); setPage(0); }}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            {STATUS_OPTIONS.map((o) => (
              <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
            ))}
          </SelectContent>
        </Select>

        {!isLoading && (
          <span className="text-sm text-muted-foreground ml-auto">
            {totalElements} user{totalElements !== 1 ? "s" : ""} found
          </span>
        )}
      </div>

      {/* Table */}
      <div className="rounded-lg border bg-card">
        {isError ? (
          <div className="flex items-center gap-2 p-8 text-sm text-destructive justify-center">
            <AlertCircle className="h-4 w-4" />
            Failed to load users. Check your connection or token.
          </div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Phone</TableHead>
                <TableHead>Role</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Joined</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading
                ? Array.from({ length: 8 }).map((_, i) => (
                    <TableRow key={i}>
                      {Array.from({ length: 7 }).map((_, j) => (
                        <TableCell key={j}>
                          <Skeleton className="h-4 w-full" />
                        </TableCell>
                      ))}
                    </TableRow>
                  ))
                : users.length === 0
                ? (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-12 text-muted-foreground">
                        No users found for the selected filters.
                      </TableCell>
                    </TableRow>
                  )
                : users.map((user) => (
                    <TableRow key={user.userId}>
                      <TableCell className="font-medium">{user.fullName}</TableCell>
                      <TableCell className="text-muted-foreground">{user.email}</TableCell>
                      <TableCell className="text-muted-foreground">{user.phone || "—"}</TableCell>
                      <TableCell>{roleBadge(user.role)}</TableCell>
                      <TableCell>{statusBadge(user.verificationStatus)}</TableCell>
                      <TableCell className="text-muted-foreground text-xs">
                        {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : "—"}
                      </TableCell>
                      <TableCell className="text-right">
                        {user.role !== "ADMIN" && (
                          <>
                            {user.verificationStatus === "SUSPENDED" ? (
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => setConfirmUser({ user, action: "activate" })}
                              >
                                Activate
                              </Button>
                            ) : (
                              <Button
                                size="sm"
                                variant="outline"
                                className="text-destructive hover:text-destructive"
                                onClick={() => setConfirmUser({ user, action: "deactivate" })}
                              >
                                Suspend
                              </Button>
                            )}
                          </>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
            </TableBody>
          </Table>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between">
          <p className="text-sm text-muted-foreground">
            Page {page + 1} of {totalPages}
          </p>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
            >
              <ChevronLeft className="h-4 w-4" />
              Previous
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}

      {/* Confirm Dialog */}
      <Dialog open={!!confirmUser} onOpenChange={() => setConfirmUser(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {confirmUser?.action === "activate" ? "Activate User" : "Suspend User"}
            </DialogTitle>
            <DialogDescription>
              {confirmUser?.action === "activate"
                ? `Are you sure you want to activate ${confirmUser?.user.fullName}? They will regain access to the platform.`
                : `Are you sure you want to suspend ${confirmUser?.user.fullName}? They will lose access to the platform.`}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirmUser(null)}>
              Cancel
            </Button>
            <Button
              variant={confirmUser?.action === "deactivate" ? "destructive" : "default"}
              onClick={handleConfirm}
              disabled={activate.isPending || deactivate.isPending}
            >
              {activate.isPending || deactivate.isPending ? "Processing..." : "Confirm"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
