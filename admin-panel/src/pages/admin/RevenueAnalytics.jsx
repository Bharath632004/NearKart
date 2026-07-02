import React, { useState, useEffect } from "react";
import {
  AreaChart, Area, BarChart, Bar, LineChart, Line,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, Cell, PieChart, Pie
} from "recharts";
import API from "../../api/axios";

const mockRevenue = [
  { month: "Jan", gross: 185000, net: 148000, refunds: 8500, commission: 18500 },
  { month: "Feb", gross: 224000, net: 179200, refunds: 10200, commission: 22400 },
  { month: "Mar", gross: 198000, net: 158400, refunds: 9100, commission: 19800 },
  { month: "Apr", gross: 312000, net: 249600, refunds: 14200, commission: 31200 },
  { month: "May", gross: 389000, net: 311200, refunds: 17800, commission: 38900 },
  { month: "Jun", gross: 456000, net: 364800, refunds: 20400, commission: 45600 },
];

const mockTopMerchants = [
  { name: "FreshVeggies", revenue: 98400, orders: 342, growth: "+12%" },
  { name: "Dairy Fresh", revenue: 76200, orders: 289, growth: "+8%" },
  { name: "Grocery Hub", revenue: 54100, orders: 198, growth: "+15%" },
  { name: "QuickMart", revenue: 39800, orders: 145, growth: "-3%" },
  { name: "SpiceWorld", revenue: 32100, orders: 121, growth: "+21%" },
];

const mockPaymentSplit = [
  { name: "UPI", value: 52 },
  { name: "Card", value: 28 },
  { name: "COD", value: 14 },
  { name: "Wallet", value: 6 },
];

const COLORS = ["#4F46E5", "#10B981", "#F59E0B", "#EF4444"];

const fmt = (v) => `₹${(v / 1000).toFixed(0)}K`;

export default function RevenueAnalytics() {
  const [revenue, setRevenue] = useState(mockRevenue);
  const [topMerchants] = useState(mockTopMerchants);
  const [paymentSplit] = useState(mockPaymentSplit);
  const [period, setPeriod] = useState("monthly");

  useEffect(() => {
    API.get("/admin/analytics/revenue", { params: { period } })
      .then(res => setRevenue(res.data.monthly || mockRevenue))
      .catch(() => {});
  }, [period]);

  const totalGross = revenue.reduce((s, r) => s + r.gross, 0);
  const totalNet = revenue.reduce((s, r) => s + r.net, 0);
  const totalRefunds = revenue.reduce((s, r) => s + r.refunds, 0);
  const totalCommission = revenue.reduce((s, r) => s + r.commission, 0);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Revenue Analytics</h2>
        <select value={period} onChange={e => setPeriod(e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm">
          <option value="monthly">Monthly</option>
          <option value="weekly">Weekly</option>
          <option value="daily">Daily</option>
        </select>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
        {[
          { label: "Gross Revenue", value: `₹${(totalGross/100000).toFixed(1)}L`, trend: "+18%", color: "bg-indigo-50 border-indigo-200 text-indigo-700" },
          { label: "Net Revenue", value: `₹${(totalNet/100000).toFixed(1)}L`, trend: "+16%", color: "bg-green-50 border-green-200 text-green-700" },
          { label: "Total Refunds", value: `₹${(totalRefunds/1000).toFixed(0)}K`, trend: "+5%", color: "bg-red-50 border-red-200 text-red-700" },
          { label: "Commission Earned", value: `₹${(totalCommission/1000).toFixed(0)}K`, trend: "+18%", color: "bg-purple-50 border-purple-200 text-purple-700" },
        ].map(c => (
          <div key={c.label} className={`rounded-xl border p-4 ${c.color}`}>
            <p className="text-xs font-medium mb-1">{c.label}</p>
            <p className="text-xl font-bold">{c.value}</p>
            <p className="text-xs mt-1 opacity-70">{c.trend} vs last period</p>
          </div>
        ))}
      </div>

      {/* Revenue Trend Chart */}
      <div className="bg-white rounded-xl shadow p-6 mb-6">
        <h3 className="text-lg font-semibold text-gray-700 mb-4">Revenue Breakdown</h3>
        <ResponsiveContainer width="100%" height={280}>
          <AreaChart data={revenue}>
            <defs>
              <linearGradient id="grossGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#4F46E5" stopOpacity={0.2} />
                <stop offset="95%" stopColor="#4F46E5" stopOpacity={0} />
              </linearGradient>
              <linearGradient id="netGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#10B981" stopOpacity={0.2} />
                <stop offset="95%" stopColor="#10B981" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="month" />
            <YAxis tickFormatter={fmt} />
            <Tooltip formatter={(v) => `₹${v.toLocaleString()}`} />
            <Legend />
            <Area type="monotone" dataKey="gross" name="Gross Revenue" stroke="#4F46E5" fill="url(#grossGrad)" strokeWidth={2} />
            <Area type="monotone" dataKey="net" name="Net Revenue" stroke="#10B981" fill="url(#netGrad)" strokeWidth={2} />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        {/* Refunds vs Commission */}
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Refunds vs Commission</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={revenue}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis tickFormatter={fmt} />
              <Tooltip formatter={v => `₹${v.toLocaleString()}`} />
              <Legend />
              <Bar dataKey="refunds" name="Refunds" fill="#EF4444" radius={[4, 4, 0, 0]} />
              <Bar dataKey="commission" name="Commission" fill="#8B5CF6" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Payment Split Pie */}
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Payment Method Split</h3>
          <div className="flex items-center justify-center gap-6">
            <ResponsiveContainer width={180} height={180}>
              <PieChart>
                <Pie data={paymentSplit} cx="50%" cy="50%" innerRadius={50} outerRadius={80} dataKey="value">
                  {paymentSplit.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip formatter={v => `${v}%`} />
              </PieChart>
            </ResponsiveContainer>
            <div className="space-y-2">
              {paymentSplit.map((p, i) => (
                <div key={p.name} className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[i] }}></div>
                  <span className="text-sm text-gray-700">{p.name}</span>
                  <span className="text-sm font-bold text-gray-800">{p.value}%</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Top Merchants */}
      <div className="bg-white rounded-xl shadow p-6">
        <h3 className="text-lg font-semibold text-gray-700 mb-4">Top Revenue Merchants</h3>
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Rank", "Merchant", "Revenue", "Orders", "Growth"].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
          </thead>
          <tbody>
            {topMerchants.map((m, i) => (
              <tr key={m.name} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3">
                  <span className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold ${
                    i === 0 ? "bg-yellow-100 text-yellow-700" : i === 1 ? "bg-gray-100 text-gray-600" : "bg-orange-50 text-orange-600"
                  }`}>#{i + 1}</span>
                </td>
                <td className="px-4 py-3 font-medium">{m.name}</td>
                <td className="px-4 py-3 font-semibold text-green-700">₹{m.revenue.toLocaleString()}</td>
                <td className="px-4 py-3">{m.orders}</td>
                <td className="px-4 py-3">
                  <span className={`text-xs font-semibold ${
                    m.growth.startsWith("+") ? "text-green-600" : "text-red-500"
                  }`}>{m.growth}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
