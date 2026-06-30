import React from "react";
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from "recharts";

const revenueData = [
  { month: "Jan", revenue: 45000 }, { month: "Feb", revenue: 62000 },
  { month: "Mar", revenue: 58000 }, { month: "Apr", revenue: 71000 },
  { month: "May", revenue: 89000 }, { month: "Jun", revenue: 102000 },
];

const categoryData = [
  { name: "Groceries", value: 45 }, { name: "Dairy", value: 25 },
  { name: "Snacks", value: 15 }, { name: "Others", value: 15 },
];

const COLORS = ["#4F46E5", "#7C3AED", "#10B981", "#F59E0B"];

export default function Analytics() {
  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Analytics Reports</h2>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="font-semibold text-gray-700 mb-4">Monthly Revenue (₹)</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={revenueData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip formatter={(v) => `₹${v.toLocaleString()}`} />
              <Bar dataKey="revenue" fill="#4F46E5" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="font-semibold text-gray-700 mb-4">Category Distribution</h3>
          <ResponsiveContainer width="100%" height={250}>
            <PieChart>
              <Pie data={categoryData} cx="50%" cy="50%" outerRadius={90} dataKey="value" label={({ name, value }) => `${name} ${value}%`}>
                {categoryData.map((_, i) => <Cell key={i} fill={COLORS[i]} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
        <div className="bg-white rounded-xl shadow p-6 lg:col-span-2">
          <h3 className="font-semibold text-gray-700 mb-4">Revenue Trend</h3>
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={revenueData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip formatter={(v) => `₹${v.toLocaleString()}`} />
              <Legend />
              <Line type="monotone" dataKey="revenue" stroke="#4F46E5" strokeWidth={3} dot={{ r: 5 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
