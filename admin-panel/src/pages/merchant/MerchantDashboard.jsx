import React, { useState } from "react";
import StatCard from "../../components/StatCard";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

const mockData = { todayOrders: 18, revenue: "₹4,320", pending: 3, products: 45 };
const mockChart = [
  { day: "Mon", sales: 3200 }, { day: "Tue", sales: 4500 },
  { day: "Wed", sales: 3800 }, { day: "Thu", sales: 5200 },
  { day: "Fri", sales: 6100 }, { day: "Sat", sales: 7800 },
];

export default function MerchantDashboard() {
  const [stats] = useState(mockData);
  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Merchant Dashboard</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard title="Today's Orders" value={stats.todayOrders} icon="📦" color="indigo" />
        <StatCard title="Revenue Today" value={stats.revenue} icon="💰" color="green" />
        <StatCard title="Pending Orders" value={stats.pending} icon="⏳" color="yellow" />
        <StatCard title="Total Products" value={stats.products} icon="🛒" color="indigo" />
      </div>
      <div className="bg-white rounded-xl shadow p-6">
        <h3 className="font-semibold text-gray-700 mb-4">This Week's Sales (₹)</h3>
        <ResponsiveContainer width="100%" height={250}>
          <BarChart data={mockChart}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="day" />
            <YAxis />
            <Tooltip formatter={(v) => `₹${v}`} />
            <Bar dataKey="sales" fill="#7C3AED" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
