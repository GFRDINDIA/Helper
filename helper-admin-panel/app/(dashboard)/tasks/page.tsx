"use client";

import { useTaskStats } from "@/lib/hooks/use-dashboard-stats";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { ClipboardList, AlertCircle, TrendingUp } from "lucide-react";

const STATUS_COLORS: Record<string, string> = {
  OPEN: "bg-blue-100 text-blue-800",
  IN_PROGRESS: "bg-yellow-100 text-yellow-800",
  COMPLETED: "bg-green-100 text-green-800",
  CANCELLED: "bg-gray-100 text-gray-800",
  DISPUTED: "bg-red-100 text-red-800",
};

function StatusBadge({ status }: { status: string }) {
  const cls = STATUS_COLORS[status] ?? "bg-gray-100 text-gray-800";
  return (
    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${cls}`}>
      {status.replace("_", " ")}
    </span>
  );
}

function pct(value: number, total: number) {
  if (!total) return "0%";
  return `${Math.round((value / total) * 100)}%`;
}

export default function TasksPage() {
  const { data, isLoading, isError } = useTaskStats();

  const statusEntries = data?.tasksByStatus
    ? Object.entries(data.tasksByStatus).sort(([, a], [, b]) => b - a)
    : [];

  const domainEntries = data?.tasksByDomain
    ? Object.entries(data.tasksByDomain).sort(([, a], [, b]) => b - a)
    : [];

  const total = data?.totalTasks ?? 0;

  if (isError) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2">
          <ClipboardList className="h-6 w-6 text-muted-foreground" />
          <h1 className="text-2xl font-bold tracking-tight">Tasks</h1>
        </div>
        <div className="flex items-center gap-2 p-8 text-sm text-destructive justify-center rounded-lg border">
          <AlertCircle className="h-4 w-4" />
          Failed to load task statistics.
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <div className="flex items-center gap-2">
          <ClipboardList className="h-6 w-6 text-muted-foreground" />
          <h1 className="text-2xl font-bold tracking-tight">Tasks</h1>
        </div>
        <p className="text-muted-foreground mt-1">
          Platform-wide task statistics broken down by status and service domain.
        </p>
      </div>

      {/* Total */}
      <div className="rounded-lg border bg-card p-6">
        <div className="flex items-center gap-3">
          <TrendingUp className="h-8 w-8 text-primary" />
          <div>
            <p className="text-sm text-muted-foreground">Total Tasks</p>
            {isLoading ? (
              <Skeleton className="h-8 w-24 mt-1" />
            ) : (
              <p className="text-3xl font-bold">{total.toLocaleString()}</p>
            )}
          </div>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* By Status */}
        <div className="space-y-3">
          <h2 className="text-lg font-semibold">By Status</h2>
          <div className="rounded-lg border bg-card">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Count</TableHead>
                  <TableHead className="text-right">Share</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading
                  ? Array.from({ length: 5 }).map((_, i) => (
                      <TableRow key={i}>
                        <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-12 ml-auto" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-10 ml-auto" /></TableCell>
                      </TableRow>
                    ))
                  : statusEntries.length === 0
                  ? (
                      <TableRow>
                        <TableCell colSpan={3} className="text-center py-8 text-muted-foreground">
                          No data available.
                        </TableCell>
                      </TableRow>
                    )
                  : statusEntries.map(([status, count]) => (
                      <TableRow key={status}>
                        <TableCell>
                          <StatusBadge status={status} />
                        </TableCell>
                        <TableCell className="text-right font-medium">
                          {count.toLocaleString()}
                        </TableCell>
                        <TableCell className="text-right text-muted-foreground text-sm">
                          {pct(count, total)}
                        </TableCell>
                      </TableRow>
                    ))}
              </TableBody>
            </Table>
          </div>
        </div>

        {/* By Domain */}
        <div className="space-y-3">
          <h2 className="text-lg font-semibold">By Service Domain</h2>
          <div className="rounded-lg border bg-card">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Domain</TableHead>
                  <TableHead className="text-right">Count</TableHead>
                  <TableHead className="text-right">Share</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading
                  ? Array.from({ length: 5 }).map((_, i) => (
                      <TableRow key={i}>
                        <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-12 ml-auto" /></TableCell>
                        <TableCell><Skeleton className="h-4 w-10 ml-auto" /></TableCell>
                      </TableRow>
                    ))
                  : domainEntries.length === 0
                  ? (
                      <TableRow>
                        <TableCell colSpan={3} className="text-center py-8 text-muted-foreground">
                          No data available.
                        </TableCell>
                      </TableRow>
                    )
                  : domainEntries.map(([domain, count]) => (
                      <TableRow key={domain}>
                        <TableCell className="font-medium capitalize">
                          {domain.replace(/_/g, " ").toLowerCase()}
                        </TableCell>
                        <TableCell className="text-right font-medium">
                          {count.toLocaleString()}
                        </TableCell>
                        <TableCell className="text-right text-muted-foreground text-sm">
                          {pct(count, total)}
                        </TableCell>
                      </TableRow>
                    ))}
              </TableBody>
            </Table>
          </div>
        </div>
      </div>
    </div>
  );
}
