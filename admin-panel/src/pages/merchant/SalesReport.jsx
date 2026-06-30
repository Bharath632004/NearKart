import React from "react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

const data = [
  { week: "W1", sales: 12500, orders: 42 },
  { week: "W2", sales: 18200, orders: 61 },
  { week: "W3", sales: 15800, orders: 53 },
  { week: "W4", sales: 22100, orders: 74 },
];

export default function SalesReport() {
  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Sales Reports</h2>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="font-semibold text-gray-700 mb-4">Weekly Revenue (₹)</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={data}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="week" /><YAxis />
              <Tooltip formatter={(v) => `₹${v}`} />
              <Bar dataKey="sales" fill="#10B981" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="font-semibold text-gray-700 mb-4">Orders per Week</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={data}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="week" /><YAxis />
              <Tooltip />
              <Bar dataKey="orders" fill="#7C3AED" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="bg-white rounded-xl shadow p-6 lg:col-span-2">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
              <tr>{["Week", "Orders", "Revenue", "Avg Order Value"].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
            </thead>
            <tbody>
              {data.map(d => (
                <tr key={d.week} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-3 font-semibold">{d.week}</td>
                  <td className="px-4 py-3">{d.orders}</td>
                  <td className="px-4 py-3 font-semibold text-green-700">₹{d.sales.toLocaleString()}</td>
                  <td className="px-4 py-3">₹{Math.round(d.sales / d.orders)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
