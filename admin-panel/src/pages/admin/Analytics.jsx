import React, { useEffect, useState } from "react";
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from "recharts";
import API from "../../api/axios";

const mockMonthly = [
  { month: "Jan", orders: 300, revenue: 45000 },
  { month: "Feb", orders: 450, revenue: 67000 },
  { month: "Mar", orders: 400, revenue: 60000 },
  { month: "Apr", orders: 600, revenue: 90000 },
  { month: "May", orders: 750, revenue: 112000 },
  { month: "Jun", orders: 900, revenue: 135000 },
];

const mockCategory = [
  { name: "Groceries", value: 45 },
  { name: "Dairy", value: 25 },
  { name: "Vegetables", value: 20 },
  { name: "Snacks", value: 10 },
];

const COLORS = ["#4F46E5", "#10B981", "#F59E0B", "#EF4444"];

export default function Analytics() {
  const [monthly, setMonthly] = useState(mockMonthly);
  const [category] = useState(mockCategory);

  useEffect(() => {
    API.get("/admin/analytics").then(res => setMonthly(res.data.monthly || mockMonthly)).catch(() => {});
  }, []);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Platform Analytics</h2>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Monthly Orders</h3>
          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={monthly}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="orders" stroke="#4F46E5" strokeWidth={2} dot={{ r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Monthly Revenue (₹)</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={monthly}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip formatter={(v) => `₹${v.toLocaleString()}`} />
              <Legend />
              <Bar dataKey="revenue" fill="#10B981" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow p-6">
        <h3 className="text-lg font-semibold text-gray-700 mb-4">Sales by Category</h3>
        <div className="flex flex-col sm:flex-row items-center justify-center gap-8">
          <ResponsiveContainer width={260} height={260}>
            <PieChart>
              <Pie data={category} cx="50%" cy="50%" outerRadius={100} dataKey="value"
                label={({ name, value }) => `${name} ${value}%`}>
                {category.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
          <div className="flex flex-col gap-2">
            {category.map((c, i) => (
              <div key={c.name} className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[i] }}></div>
                <span className="text-sm text-gray-600">{c.name}: <strong>{c.value}%</strong></span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
