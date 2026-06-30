import React, { useEffect, useState } from "react";
import StatCard from "../../components/StatCard";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import API from "../../api/axios";

const mockStats = { users: 1240, merchants: 87, orders: 3421, revenue: "₹4,56,200" };
const mockChart = [
  { month: "Jan", orders: 300 }, { month: "Feb", orders: 450 },
  { month: "Mar", orders: 400 }, { month: "Apr", orders: 600 },
  { month: "May", orders: 750 }, { month: "Jun", orders: 900 },
];

export default function Dashboard() {
  const [stats, setStats] = useState(mockStats);
  const [chartData] = useState(mockChart);

  useEffect(() => {
    API.get("/admin/stats").then(res => setStats(res.data)).catch(() => {});
  }, []);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Platform Dashboard</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard title="Total Users" value={stats.users} icon="👤" color="indigo" />
        <StatCard title="Merchants" value={stats.merchants} icon="🏪" color="green" />
        <StatCard title="Orders Today" value={stats.orders} icon="📦" color="yellow" />
        <StatCard title="Revenue" value={stats.revenue} icon="💰" color="green" />
      </div>
      <div className="bg-white rounded-xl shadow p-6">
        <h3 className="text-lg font-semibold text-gray-700 mb-4">Monthly Orders</h3>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="month" />
            <YAxis />
            <Tooltip />
            <Line type="monotone" dataKey="orders" stroke="#4F46E5" strokeWidth={2} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
