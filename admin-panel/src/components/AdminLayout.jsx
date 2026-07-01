import React, { useState } from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";
import Navbar from "./Navbar";

const adminLinks = [
  { to: "/admin", label: "Dashboard", icon: "🏠", end: true },
  { to: "/admin/users", label: "Users", icon: "👥" },
  { to: "/admin/merchants", label: "Merchants", icon: "🏪" },
  { to: "/admin/delivery", label: "Delivery Partners", icon: "🚚" },
  { to: "/admin/orders", label: "Orders", icon: "📦" },
  { to: "/admin/payments", label: "Payments", icon: "💳" },
  { to: "/admin/coupons", label: "Coupons", icon: "🎟️" },
  { to: "/admin/analytics", label: "Analytics", icon: "📊" },
  { to: "/admin/settings", label: "Settings", icon: "⚙️" },
];

export default function AdminLayout() {
  const [open, setOpen] = useState(true);
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar links={adminLinks} open={open} title="NearKart Admin" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Navbar onToggle={() => setOpen(!open)} />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
