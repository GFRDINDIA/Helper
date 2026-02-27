"use client";

import { useState } from "react";
import { useKycQueue, useReviewKyc } from "@/lib/hooks/use-kyc";
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
import { FileCheck, ExternalLink, AlertCircle, CheckCircle, XCircle } from "lucide-react";
import type { KycDocument } from "@/lib/types";

const STATUS_OPTIONS = [
  { value: "PENDING", label: "Pending" },
  { value: "APPROVED", label: "Approved" },
  { value: "REJECTED", label: "Rejected" },
];

function statusBadge(status: string) {
  const map: Record<string, string> = {
    PENDING: "bg-yellow-100 text-yellow-800",
    APPROVED: "bg-green-100 text-green-800",
    REJECTED: "bg-red-100 text-red-800",
  };
  return (
    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${map[status] ?? "bg-gray-100 text-gray-800"}`}>
      {status}
    </span>
  );
}

export default function KycPage() {
  const [statusFilter, setStatusFilter] = useState("PENDING");
  const [selected, setSelected] = useState<KycDocument | null>(null);
  const [action, setAction] = useState<"APPROVED" | "REJECTED" | null>(null);
  const [adminNotes, setAdminNotes] = useState("");

  const { data: documents = [], isLoading, isError } = useKycQueue(statusFilter);
  const reviewKyc = useReviewKyc();

  const openDialog = (doc: KycDocument, act: "APPROVED" | "REJECTED") => {
    setSelected(doc);
    setAction(act);
    setAdminNotes("");
  };

  const handleReview = () => {
    if (!selected || !action) return;
    reviewKyc.mutate(
      { documentId: selected.id, payload: { status: action, adminNotes: adminNotes || undefined } },
      { onSuccess: () => { setSelected(null); setAction(null); } }
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <div className="flex items-center gap-2">
          <FileCheck className="h-6 w-6 text-muted-foreground" />
          <h1 className="text-2xl font-bold tracking-tight">KYC Approvals</h1>
        </div>
        <p className="text-muted-foreground mt-1">
          Review and approve or reject identity verification documents.
        </p>
      </div>

      {/* Filter */}
      <div className="flex items-center gap-3">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[160px]">
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
            {documents.length} document{documents.length !== 1 ? "s" : ""}
          </span>
        )}
      </div>

      {/* Table */}
      <div className="rounded-lg border bg-card">
        {isError ? (
          <div className="flex items-center gap-2 p-8 text-sm text-destructive justify-center">
            <AlertCircle className="h-4 w-4" />
            Failed to load KYC documents.
          </div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>User</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Document Type</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Submitted</TableHead>
                <TableHead>Document</TableHead>
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
                : documents.length === 0
                ? (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-12 text-muted-foreground">
                        No {statusFilter.toLowerCase()} KYC documents.
                      </TableCell>
                    </TableRow>
                  )
                : documents.map((doc) => (
                    <TableRow key={doc.id}>
                      <TableCell className="font-medium">{doc.userName}</TableCell>
                      <TableCell className="text-muted-foreground">{doc.email}</TableCell>
                      <TableCell>{doc.documentType}</TableCell>
                      <TableCell>{statusBadge(doc.status)}</TableCell>
                      <TableCell className="text-muted-foreground text-xs">
                        {new Date(doc.submittedAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        <a
                          href={doc.documentUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="inline-flex items-center gap-1 text-xs text-primary hover:underline"
                        >
                          View <ExternalLink className="h-3 w-3" />
                        </a>
                      </TableCell>
                      <TableCell className="text-right">
                        {doc.status === "PENDING" && (
                          <div className="flex items-center gap-2 justify-end">
                            <Button
                              size="sm"
                              variant="outline"
                              className="text-green-700 hover:text-green-700 border-green-200 hover:bg-green-50"
                              onClick={() => openDialog(doc, "APPROVED")}
                            >
                              <CheckCircle className="h-3 w-3 mr-1" />
                              Approve
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              className="text-destructive hover:text-destructive border-red-200 hover:bg-red-50"
                              onClick={() => openDialog(doc, "REJECTED")}
                            >
                              <XCircle className="h-3 w-3 mr-1" />
                              Reject
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
              {action === "APPROVED" ? "Approve KYC Document" : "Reject KYC Document"}
            </DialogTitle>
            <DialogDescription>
              {action === "APPROVED"
                ? `Approve identity document for ${selected?.userName}. This will verify their account.`
                : `Reject identity document for ${selected?.userName}. Please provide a reason.`}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-3 py-2">
            <div className="text-sm">
              <span className="text-muted-foreground">Document type: </span>
              <span className="font-medium">{selected?.documentType}</span>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium">
                Admin Notes {action === "REJECTED" && <span className="text-destructive">*</span>}
              </label>
              <Textarea
                placeholder={action === "REJECTED" ? "Reason for rejection..." : "Optional notes..."}
                value={adminNotes}
                onChange={(e) => setAdminNotes(e.target.value)}
                rows={3}
              />
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setSelected(null)}>
              Cancel
            </Button>
            <Button
              variant={action === "REJECTED" ? "destructive" : "default"}
              onClick={handleReview}
              disabled={reviewKyc.isPending || (action === "REJECTED" && !adminNotes.trim())}
            >
              {reviewKyc.isPending
                ? "Processing..."
                : action === "APPROVED"
                ? "Approve"
                : "Reject"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
