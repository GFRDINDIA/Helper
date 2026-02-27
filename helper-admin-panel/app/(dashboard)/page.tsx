"use client";

import {
  Users,
  UserCheck,
  Briefcase,
  ClipboardList,
  FileCheck,
  Flag,
  IndianRupee,
  TrendingUp,
  Star,
  Bell,
  ShieldCheck,
  Activity,
} from "lucide-react";
import { StatCard } from "@/components/dashboard/stat-card";
import {
  useUserStats,
  useTaskStats,
  usePaymentStats,
  useRatingStats,
  useKycStats,
  useNotificationStats,
} from "@/lib/hooks/use-dashboard-stats";

function SectionHeading({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="mb-4">
      <h2 className="text-lg font-semibold text-slate-800">{title}</h2>
      {subtitle && <p className="text-sm text-slate-500">{subtitle}</p>}
    </div>
  );
}

export default function DashboardPage() {
  const userStats = useUserStats();
  const taskStats = useTaskStats();
  const paymentStats = usePaymentStats();
  const ratingStats = useRatingStats();
  const kycStats = useKycStats();
  const notificationStats = useNotificationStats();

  return (
    <div className="space-y-8">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Platform Overview</h1>
        <p className="text-slate-500 mt-1">
          Live statistics across all Helper services
        </p>
      </div>

      {/* ── Section 1: Users ── */}
      <section>
        <SectionHeading title="Users" subtitle="Registered accounts by role" />
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            title="Total Users"
            value={userStats.data?.totalUsers}
            description="All registered accounts"
            icon={Users}
            loading={userStats.isLoading}
            error={userStats.isError}
          />
          <StatCard
            title="Customers"
            value={userStats.data?.totalCustomers}
            description="Task posters"
            icon={UserCheck}
            iconColor="text-blue-500"
            loading={userStats.isLoading}
            error={userStats.isError}
          />
          <StatCard
            title="Workers"
            value={userStats.data?.totalWorkers}
            description="Service providers"
            icon={Briefcase}
            iconColor="text-emerald-500"
            loading={userStats.isLoading}
            error={userStats.isError}
          />
          <StatCard
            title="Admins"
            value={userStats.data?.totalAdmins}
            description="Platform administrators"
            icon={ShieldCheck}
            iconColor="text-violet-500"
            loading={userStats.isLoading}
            error={userStats.isError}
          />
        </div>
      </section>

      {/* ── Section 2: Operations ── */}
      <section>
        <SectionHeading title="Operations" subtitle="Active tasks, KYC queue, and content flags" />
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <StatCard
            title="Total Tasks"
            value={taskStats.data?.totalTasks}
            description="All platform tasks"
            icon={ClipboardList}
            iconColor="text-orange-500"
            loading={taskStats.isLoading}
            error={taskStats.isError}
          />
          <StatCard
            title="Pending KYC"
            value={kycStats.data?.pending}
            description="Awaiting admin review"
            icon={FileCheck}
            iconColor="text-amber-500"
            loading={kycStats.isLoading}
            error={kycStats.isError}
          />
          <StatCard
            title="Pending Flags"
            value={ratingStats.data?.pendingFlags}
            description="Content flags to review"
            icon={Flag}
            iconColor="text-red-500"
            loading={ratingStats.isLoading}
            error={ratingStats.isError}
          />
        </div>
      </section>

      {/* ── Section 3: Financial ── */}
      <section>
        <SectionHeading title="Financial" subtitle="Revenue and commission overview" />
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            title="Total Revenue"
            value={
              paymentStats.data?.totalRevenue !== undefined
                ? `₹${Number(paymentStats.data.totalRevenue).toLocaleString("en-IN", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}`
                : null
            }
            description="Gross transaction value"
            icon={IndianRupee}
            iconColor="text-green-600"
            loading={paymentStats.isLoading}
            error={paymentStats.isError}
          />
          <StatCard
            title="Commission Earned"
            value={
              paymentStats.data?.totalCommission !== undefined
                ? `₹${Number(paymentStats.data.totalCommission).toLocaleString("en-IN", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}`
                : null
            }
            description="2% platform commission"
            icon={TrendingUp}
            iconColor="text-teal-500"
            loading={paymentStats.isLoading}
            error={paymentStats.isError}
          />
          <StatCard
            title="GST Collected"
            value={
              paymentStats.data?.totalTax !== undefined
                ? `₹${Number(paymentStats.data.totalTax).toLocaleString("en-IN", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}`
                : null
            }
            description="18% on platform commission"
            icon={IndianRupee}
            iconColor="text-slate-500"
            loading={paymentStats.isLoading}
            error={paymentStats.isError}
          />
          <StatCard
            title="Tips Collected"
            value={
              paymentStats.data?.totalTips !== undefined
                ? `₹${Number(paymentStats.data.totalTips).toLocaleString("en-IN", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}`
                : null
            }
            description="100% passed to workers"
            icon={IndianRupee}
            iconColor="text-pink-500"
            loading={paymentStats.isLoading}
            error={paymentStats.isError}
          />
        </div>
      </section>

      {/* ── Section 4: Platform Health ── */}
      <section>
        <SectionHeading title="Platform Health" subtitle="Ratings and notification delivery" />
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            title="Total Ratings"
            value={ratingStats.data?.totalRatings}
            description="Submitted by both parties"
            icon={Star}
            iconColor="text-yellow-500"
            loading={ratingStats.isLoading}
            error={ratingStats.isError}
          />
          <StatCard
            title="Avg. Platform Score"
            value={
              ratingStats.data?.platformAverage !== undefined
                ? `${Number(ratingStats.data.platformAverage).toFixed(2)} / 5`
                : null
            }
            description="Weighted average rating"
            icon={Activity}
            iconColor="text-cyan-500"
            loading={ratingStats.isLoading}
            error={ratingStats.isError}
          />
          <StatCard
            title="Notifications Sent"
            value={notificationStats.data?.sent}
            description="Delivered to users"
            icon={Bell}
            iconColor="text-indigo-500"
            loading={notificationStats.isLoading}
            error={notificationStats.isError}
          />
          <StatCard
            title="Active Device Tokens"
            value={notificationStats.data?.activeDeviceTokens}
            description="Push-enabled devices"
            icon={Bell}
            iconColor="text-slate-400"
            loading={notificationStats.isLoading}
            error={notificationStats.isError}
          />
        </div>
      </section>
    </div>
  );
}
