import React, { useEffect, useState } from "react";
import { BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import API from "../../api/axios";

const mockData = [
  { week: "W1", sales: 4500, orders: 18 },
  { week: "W2", sales: 6800, orders: 27 },
  { week: "W3", sales: 5200, orders: 21 },
  { week: "W4", sales: 9100, orders: 36 },
];

const mockTopProducts = [
  { name: "Tomatoes", sold: 120, revenue: 3600 },
  { name: "Milk 500ml", sold: 95, revenue: 2375 },
  { name: "Basmati Rice", sold: 60, revenue: 7200 },
];

export default function SalesReport() {
  const [data, setData] = useState(mockData);
  const [topProducts] = useState(mockTopProducts);

  useEffect(() => {
    API.get("/merchant/sales-report").then(res => setData(res.data.weekly || mockData)).catch(() => {});
  }, []);

  const totalRevenue = data.reduce((s, d) => s + d.sales, 0);
  const totalOrders = data.reduce((s, d) => s + d.orders, 0);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Sales Report</h2>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
        {[
          { label: "Total Revenue", value: `₹${totalRevenue.toLocaleString()}`, color: "text-green-600" },
          { label: "Total Orders", value: totalOrders, color: "text-indigo-600" },
          { label: "Avg Order Value", value: `₹${Math.round(totalRevenue / totalOrders)}`, color: "text-yellow-600" },
          { label: "Active Weeks", value: data.length, color: "text-purple-600" },
        ].map(c => (
          <div key={c.label} className="bg-white rounded-xl shadow p-4 text-center">
            <p className="text-xs text-gray-500 mb-1">{c.label}</p>
            <p className={`text-xl font-bold ${c.color}`}>{c.value}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Weekly Revenue (₹)</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={data}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="week" />
              <YAxis />
              <Tooltip formatter={v => `₹${v}`} />
              <Bar dataKey="sales" fill="#10B981" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Weekly Orders</h3>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={data}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="week" />
              <YAxis />
              <Tooltip />
              <Line type="monotone" dataKey="orders" stroke="#4F46E5" strokeWidth={2} dot={{ r: 5 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow p-6">
        <h3 className="text-lg font-semibold text-gray-700 mb-4">Top Products</h3>
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Product", "Units Sold", "Revenue"].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
          </thead>
          <tbody>
            {topProducts.map((p, i) => (
              <tr key={p.name} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-medium flex items-center gap-2">
                  <span className="bg-indigo-100 text-indigo-700 rounded-full w-6 h-6 flex items-center justify-center text-xs font-bold">#{i + 1}</span>
                  {p.name}
                </td>
                <td className="px-4 py-3">{p.sold}</td>
                <td className="px-4 py-3 font-semibold text-green-700">₹{p.revenue.toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
