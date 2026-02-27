"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Users,
  FileCheck,
  ClipboardList,
  Flag,
  Settings,
  Lock,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";

const navItems = [
  {
    href: "/",
    label: "Dashboard",
    icon: LayoutDashboard,
    available: true,
  },
  {
    href: "/users",
    label: "Users",
    icon: Users,
    available: false,
  },
  {
    href: "/kyc",
    label: "KYC Approvals",
    icon: FileCheck,
    available: false,
  },
  {
    href: "/tasks",
    label: "Tasks",
    icon: ClipboardList,
    available: false,
  },
  {
    href: "/flags",
    label: "Flags & Disputes",
    icon: Flag,
    available: false,
  },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-64 min-h-screen bg-slate-900 text-slate-100 flex flex-col">
      {/* Logo */}
      <div className="p-6 flex items-center gap-3">
        <div className="h-8 w-8 rounded-md bg-primary flex items-center justify-center">
          <span className="text-primary-foreground font-bold text-sm">H</span>
        </div>
        <div>
          <p className="font-semibold text-white">Helper</p>
          <p className="text-xs text-slate-400">Admin Panel</p>
        </div>
      </div>

      <Separator className="bg-slate-800" />

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-1">
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          const Icon = item.icon;

          return (
            <div key={item.href}>
              {item.available ? (
                <Link
                  href={item.href}
                  className={cn(
                    "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                    isActive
                      ? "bg-primary text-primary-foreground"
                      : "text-slate-300 hover:bg-slate-800 hover:text-white"
                  )}
                >
                  <Icon className="h-4 w-4" />
                  {item.label}
                </Link>
              ) : (
                <div className="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium text-slate-500 cursor-not-allowed">
                  <Icon className="h-4 w-4" />
                  {item.label}
                  <Lock className="h-3 w-3 ml-auto" />
                </div>
              )}
            </div>
          );
        })}
      </nav>

      <Separator className="bg-slate-800" />

      {/* Bottom section */}
      <div className="p-4 space-y-1">
        <div className="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium text-slate-500 cursor-not-allowed">
          <Settings className="h-4 w-4" />
          Settings
          <Lock className="h-3 w-3 ml-auto" />
        </div>
        <div className="px-3 py-2">
          <Badge variant="outline" className="text-xs border-slate-700 text-slate-400">
            Phase 1 — MVP
          </Badge>
        </div>
      </div>
    </aside>
  );
}
