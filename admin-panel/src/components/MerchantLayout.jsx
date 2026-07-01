import React, { useState } from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";
import Navbar from "./Navbar";

const merchantLinks = [
  { to: "/merchant", label: "Dashboard", icon: "🏠", end: true },
  { to: "/merchant/products", label: "Products", icon: "🛒" },
  { to: "/merchant/orders", label: "Orders", icon: "📦" },
  { to: "/merchant/sales", label: "Sales Report", icon: "📈" },
  { to: "/merchant/settlements", label: "Settlements", icon: "💰" },
];

export default function MerchantLayout() {
  const [open, setOpen] = useState(true);
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar links={merchantLinks} open={open} title="Merchant Panel" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Navbar onToggle={() => setOpen(!open)} />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
