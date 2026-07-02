import React, { useEffect, useState, useCallback } from "react";
import StatCard from "../../components/StatCard";
import { SkeletonCard } from "../../components/Skeleton";
import Toast from "../../components/Toast";
import useToast from "../../hooks/useToast";
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend, PieChart, Pie, Cell,
} from "recharts";
import API from "../../api/axios";

const MOCK_STATS = { users: 1240, merchants: 87, orders: 3421, revenue: "\u20b94,56,200", deliveryPartners: 34, activeOrders: 58 };
const MOCK_REVENUE = [
  { month: "Jan", revenue: 38000, orders: 300 },
  { month: "Feb", revenue: 52000, orders: 450 },
  { month: "Mar", revenue: 47000, orders: 400 },
  { month: "Apr", revenue: 63000, orders: 600 },
  { month: "May", revenue: 78000, orders: 750 },
  { month: "Jun", revenue: 94000, orders: 900 },
];
const MOCK_CUSTOMER_GROWTH = [
  { week: "W1", new: 45 }, { week: "W2", new: 62 },
  { week: "W3", new: 58 }, { week: "W4", new: 80 },
];
const MOCK_DELIVERY = [
  { name: "Delivered", value: 72, color: "#22c55e" },
  { name: "In Transit", value: 18, color: "#6366f1" },
  { name: "Pending", value: 10, color: "#f59e0b" },
];
const MOCK_LIVE_ORDERS = [
  { id: "o91", customer: "Ravi Kumar", merchant: "Fresh Veggies", status: "out_for_delivery", amount: 350 },
  { id: "o92", customer: "Priya S", merchant: "Daily Dairy", status: "confirmed", amount: 120 },
  { id: "o93", customer: "Anjali R", merchant: "Ravi Kirana", status: "pending", amount: 890 },
];
const MOCK_SYSTEM = [
  { label: "API Uptime", value: "99.9%", ok: true },
  { label: "DB Status", value: "Healthy", ok: true },
  { label: "Payment Gateway", value: "Online", ok: true },
  { label: "Firebase", value: "Connected", ok: true },
];
const MOCK_TOP_MERCHANTS = [
  { name: "Fresh Veggies", orders: 142, rating: 4.8 },
  { name: "Daily Dairy", orders: 98, rating: 4.6 },
  { name: "Ravi Kirana", orders: 74, rating: 4.5 },
];

const STATUS_COLORS = {
  pending: "bg-yellow-100 text-yellow-700",
  confirmed: "bg-blue-100 text-blue-700",
  out_for_delivery: "bg-indigo-100 text-indigo-700",
  delivered: "bg-green-100 text-green-700",
  cancelled: "bg-red-100 text-red-700",
};

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [revenueData] = useState(MOCK_REVENUE);
  const [customerGrowth] = useState(MOCK_CUSTOMER_GROWTH);
  const [deliveryDist] = useState(MOCK_DELIVERY);
  const [liveOrders] = useState(MOCK_LIVE_ORDERS);
  const [systemHealth] = useState(MOCK_SYSTEM);
  const [topMerchants] = useState(MOCK_TOP_MERCHANTS);
  const { toast, showToast, hideToast } = useToast();

  const fetchStats = useCallback(async () => {
    try {
      const res = await API.get("/admin/stats");
      setStats(res.data);
    } catch {
      setStats(MOCK_STATS);
    }
  }, []);

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 30000);
    return () => clearInterval(interval);
  }, [fetchStats]);

  return (
    <div className="space-y-8">
      {toast && <Toast message={toast.message} type={toast.type} onClose={hideToast} />}
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">Platform Dashboard</h2>
        <span className="text-xs text-green-600 bg-green-50 px-3 py-1 rounded-full border border-green-200 animate-pulse">
          \u25cf Live
        </span>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        {stats ? (
          <>
            <StatCard title="Total Users" value={stats.users} icon="\uD83D\uDC64" color="indigo" />
            <StatCard title="Merchants" value={stats.merchants} icon="\uD83C\uDFEA" color="green" />
            <StatCard title="Orders Today" value={stats.orders} icon="\uD83D\uDCE6" color="yellow" />
            <StatCard title="Revenue" value={stats.revenue} icon="\uD83D\uDCB0" color="green" />
            <StatCard title="Delivery Partners" value={stats.deliveryPartners ?? 34} icon="\uD83D\uDEB4" color="indigo" />
            <StatCard title="Active Orders" value={stats.activeOrders ?? 58} icon="\u23F3" color="yellow" />
          </>
        ) : (
          Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)
        )}
      </div>

      {/* Revenue + Customer Growth */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="text-base font-semibold text-gray-700 mb-4">Revenue & Orders (Monthly)</h3>
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={revenueData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" tick={{ fontSize: 12 }} />
              <YAxis yAxisId="left" tick={{ fontSize: 12 }} />
              <YAxis yAxisId="right" orientation="right" tick={{ fontSize: 12 }} />
              <Tooltip />
              <Legend />
              <Line yAxisId="left" type="monotone" dataKey="revenue" stroke="#4F46E5" strokeWidth={2} name="Revenue (\u20b9)" />
              <Line yAxisId="right" type="monotone" dataKey="orders" stroke="#22c55e" strokeWidth={2} name="Orders" />
            </LineChart>
          </ResponsiveContainer>
        </div>
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="text-base font-semibold text-gray-700 mb-4">Customer Growth (Weekly)</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={customerGrowth}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="week" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="new" fill="#6366f1" radius={[4, 4, 0, 0]} name="New Customers" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Delivery Status + Live Orders */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="text-base font-semibold text-gray-700 mb-4">Delivery Status Distribution</h3>
          <div className="flex items-center justify-center gap-8">
            <ResponsiveContainer width={200} height={200}>
              <PieChart>
                <Pie data={deliveryDist} cx="50%" cy="50%" innerRadius={55} outerRadius={80} dataKey="value">
                  {deliveryDist.map((d, i) => <Cell key={i} fill={d.color} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
            <div className="space-y-2">
              {deliveryDist.map(d => (
                <div key={d.name} className="flex items-center gap-2 text-sm">
                  <span className="w-3 h-3 rounded-full" style={{ background: d.color }} />
                  {d.name}: <b>{d.value}%</b>
                </div>
              ))}
            </div>
          </div>
        </div>
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="text-base font-semibold text-gray-700 mb-4">\u26a1 Live Orders</h3>
          <div className="space-y-3">
            {liveOrders.map(o => (
              <div key={o.id} className="flex items-center justify-between border rounded-lg px-4 py-2">
                <div>
                  <p className="font-medium text-sm">{o.customer}</p>
                  <p className="text-xs text-gray-400">{o.merchant}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-semibold">\u20b9{o.amount}</p>
                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${STATUS_COLORS[o.status] || "bg-gray-100 text-gray-600"}`}>
                    {o.status.replace(/_/g, " ")}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Top Merchants + System Health */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="text-base font-semibold text-gray-700 mb-4">\uD83C\uDFEA Active Merchants (Top)</h3>
          <table className="w-full text-sm">
            <thead className="text-xs text-gray-500 uppercase bg-gray-50">
              <tr>
                <th className="px-3 py-2 text-left">Merchant</th>
                <th className="px-3 py-2 text-left">Orders</th>
                <th className="px-3 py-2 text-left">Rating</th>
              </tr>
            </thead>
            <tbody>
              {topMerchants.map(m => (
                <tr key={m.name} className="border-t hover:bg-gray-50">
                  <td className="px-3 py-2 font-medium">{m.name}</td>
                  <td className="px-3 py-2">{m.orders}</td>
                  <td className="px-3 py-2 text-yellow-500 font-semibold">\u2605 {m.rating}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="text-base font-semibold text-gray-700 mb-4">\uD83D\uDEE1\uFE0F System Health</h3>
          <div className="grid grid-cols-2 gap-3">
            {systemHealth.map(s => (
              <div key={s.label} className={`rounded-xl border px-4 py-3 flex items-center gap-3 ${s.ok ? "border-green-200 bg-green-50" : "border-red-200 bg-red-50"}`}>
                <span className={`text-xl ${s.ok ? "text-green-500" : "text-red-500"}`}>{s.ok ? "\u2705" : "\u274C"}</span>
                <div>
                  <p className="text-xs text-gray-500">{s.label}</p>
                  <p className={`text-sm font-semibold ${s.ok ? "text-green-700" : "text-red-700"}`}>{s.value}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
