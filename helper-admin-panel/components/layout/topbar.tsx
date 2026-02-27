"use client";

import { useRouter } from "next/navigation";
import { LogOut, User } from "lucide-react";
import { Button } from "@/components/ui/button";
import { clearToken } from "@/lib/api";

interface TopbarProps {
  title: string;
}

export function Topbar({ title }: TopbarProps) {
  const router = useRouter();

  function handleLogout() {
    clearToken();
    localStorage.removeItem("helper_admin_user");
    router.push("/login");
    router.refresh();
  }

  const userStr = typeof window !== "undefined"
    ? localStorage.getItem("helper_admin_user")
    : null;
  const user = userStr ? JSON.parse(userStr) : null;

  return (
    <header className="h-16 border-b bg-white flex items-center justify-between px-6">
      <h1 className="text-xl font-semibold text-slate-800">{title}</h1>

      <div className="flex items-center gap-4">
        {user && (
          <div className="flex items-center gap-2 text-sm text-slate-600">
            <div className="h-7 w-7 rounded-full bg-primary/10 flex items-center justify-center">
              <User className="h-4 w-4 text-primary" />
            </div>
            <span className="hidden sm:inline">{user.email}</span>
          </div>
        )}
        <Button variant="ghost" size="sm" onClick={handleLogout} className="gap-2">
          <LogOut className="h-4 w-4" />
          <span className="hidden sm:inline">Logout</span>
        </Button>
      </div>
    </header>
  );
}
