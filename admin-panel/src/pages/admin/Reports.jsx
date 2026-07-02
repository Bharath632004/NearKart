import React, { useState, useEffect } from "react";
import API from "../../api/axios";

const REPORT_TYPES = [
  { id: "sales", label: "Sales Report", icon: "📈" },
  { id: "orders", label: "Orders Report", icon: "📦" },
  { id: "users", label: "User Growth Report", icon: "👥" },
  { id: "merchants", label: "Merchant Report", icon: "🏪" },
  { id: "payments", label: "Payment Report", icon: "💳" },
  { id: "delivery", label: "Delivery Report", icon: "🚚" },
];

const mockSalesReport = [
  { date: "2026-06-01", orders: 45, revenue: 12500, avgOrder: 277 },
  { date: "2026-06-02", orders: 62, revenue: 18700, avgOrder: 301 },
  { date: "2026-06-03", orders: 38, revenue: 9800, avgOrder: 257 },
  { date: "2026-06-04", orders: 71, revenue: 21200, avgOrder: 298 },
  { date: "2026-06-05", orders: 55, revenue: 16500, avgOrder: 300 },
  { date: "2026-06-06", orders: 80, revenue: 24800, avgOrder: 310 },
  { date: "2026-06-07", orders: 95, revenue: 29500, avgOrder: 310 },
];

const mockOrdersReport = [
  { status: "Delivered", count: 1245, percentage: "68.2%" },
  { status: "Cancelled", count: 187, percentage: "10.2%" },
  { status: "Pending", count: 220, percentage: "12.1%" },
  { status: "Processing", count: 178, percentage: "9.8%" },
];

const mockUsersReport = [
  { month: "Jan", newUsers: 320, activeUsers: 890, churnRate: "3.2%" },
  { month: "Feb", newUsers: 450, activeUsers: 1100, churnRate: "2.8%" },
  { month: "Mar", newUsers: 380, activeUsers: 1250, churnRate: "2.1%" },
  { month: "Apr", newUsers: 620, activeUsers: 1560, churnRate: "1.9%" },
  { month: "May", newUsers: 750, activeUsers: 1890, churnRate: "1.7%" },
  { month: "Jun", newUsers: 890, activeUsers: 2200, churnRate: "1.5%" },
];

const mockData = {
  sales: mockSalesReport,
  orders: mockOrdersReport,
  users: mockUsersReport,
  merchants: [
    { name: "FreshVeggies Store", orders: 342, revenue: 98400, rating: 4.8, status: "Active" },
    { name: "Dairy Fresh", orders: 289, revenue: 76200, rating: 4.6, status: "Active" },
    { name: "Grocery Hub", orders: 198, revenue: 54100, rating: 4.2, status: "Active" },
    { name: "QuickMart", orders: 145, revenue: 39800, rating: 3.9, status: "Warning" },
  ],
  payments: [
    { method: "UPI", count: 892, amount: 245600, successRate: "98.2%" },
    { method: "Card", count: 456, amount: 178900, successRate: "96.8%" },
    { method: "COD", count: 321, amount: 89400, successRate: "94.1%" },
    { method: "Wallet", count: 178, amount: 43200, successRate: "99.1%" },
  ],
  delivery: [
    { partner: "Ravi Kumar", deliveries: 145, avgTime: "28 min", rating: 4.9, earnings: 12500 },
    { partner: "Suresh Babu", deliveries: 132, avgTime: "31 min", rating: 4.7, earnings: 11200 },
    { partner: "Kiran Reddy", deliveries: 118, avgTime: "35 min", rating: 4.5, earnings: 9800 },
  ],
};

const COLS = {
  sales: ["Date", "Orders", "Revenue (₹)", "Avg Order (₹)"],
  orders: ["Status", "Count", "Percentage"],
  users: ["Month", "New Users", "Active Users", "Churn Rate"],
  merchants: ["Merchant", "Orders", "Revenue (₹)", "Rating", "Status"],
  payments: ["Method", "Transactions", "Amount (₹)", "Success Rate"],
  delivery: ["Partner", "Deliveries", "Avg Time", "Rating", "Earnings (₹)"],
};

const ROWKEYS = {
  sales: ["date", "orders", "revenue", "avgOrder"],
  orders: ["status", "count", "percentage"],
  users: ["month", "newUsers", "activeUsers", "churnRate"],
  merchants: ["name", "orders", "revenue", "rating", "status"],
  payments: ["method", "count", "amount", "successRate"],
  delivery: ["partner", "deliveries", "avgTime", "rating", "earnings"],
};

export default function Reports() {
  const [activeType, setActiveType] = useState("sales");
  const [data, setData] = useState(mockData.sales);
  const [loading, setLoading] = useState(false);
  const [dateFrom, setDateFrom] = useState("2026-06-01");
  const [dateTo, setDateTo] = useState("2026-06-30");

  const loadReport = (type) => {
    setActiveType(type);
    setLoading(true);
    API.get(`/admin/reports/${type}`, { params: { from: dateFrom, to: dateTo } })
      .then(res => setData(res.data))
      .catch(() => setData(mockData[type] || []))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadReport(activeType); }, []); // eslint-disable-line

  const exportCSV = () => {
    const cols = COLS[activeType];
    const keys = ROWKEYS[activeType];
    const csv = [cols.join(","), ...data.map(row => keys.map(k => row[k] ?? "").join(","))].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${activeType}-report-${dateFrom}-to-${dateTo}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const summaryCards = () => {
    if (activeType === "sales") {
      const totalRev = data.reduce((s, r) => s + (r.revenue || 0), 0);
      const totalOrd = data.reduce((s, r) => s + (r.orders || 0), 0);
      return [
        { label: "Total Revenue", value: `₹${totalRev.toLocaleString()}`, color: "bg-green-50 border-green-200 text-green-700" },
        { label: "Total Orders", value: totalOrd, color: "bg-blue-50 border-blue-200 text-blue-700" },
        { label: "Avg Daily Revenue", value: `₹${data.length ? Math.round(totalRev / data.length).toLocaleString() : 0}`, color: "bg-purple-50 border-purple-200 text-purple-700" },
        { label: "Report Days", value: data.length, color: "bg-yellow-50 border-yellow-200 text-yellow-700" },
      ];
    }
    return [];
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Reports</h2>
        <button onClick={exportCSV}
          className="flex items-center gap-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 text-sm font-semibold">
          ⬇️ Export CSV
        </button>
      </div>

      {/* Report Type Tabs */}
      <div className="flex flex-wrap gap-2 mb-6">
        {REPORT_TYPES.map(rt => (
          <button key={rt.id}
            onClick={() => loadReport(rt.id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              activeType === rt.id
                ? "bg-indigo-600 text-white shadow"
                : "bg-white text-gray-700 border border-gray-200 hover:bg-indigo-50"
            }`}>
            {rt.icon} {rt.label}
          </button>
        ))}
      </div>

      {/* Date Range Filter */}
      <div className="bg-white rounded-xl shadow p-4 mb-6 flex flex-wrap gap-4 items-end">
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">From</label>
          <input type="date" value={dateFrom} onChange={e => setDateFrom(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">To</label>
          <input type="date" value={dateTo} onChange={e => setDateTo(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
        </div>
        <button onClick={() => loadReport(activeType)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-indigo-700">
          Generate Report
        </button>
      </div>

      {/* Summary Cards */}
      {summaryCards().length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
          {summaryCards().map(c => (
            <div key={c.label} className={`rounded-xl border p-4 ${c.color}`}>
              <p className="text-xs font-medium mb-1">{c.label}</p>
              <p className="text-xl font-bold">{c.value}</p>
            </div>
          ))}
        </div>
      )}

      {/* Table */}
      <div className="bg-white rounded-xl shadow p-6">
        <h3 className="text-lg font-semibold text-gray-700 mb-4 capitalize">{activeType} Report</h3>
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
                <tr>{COLS[activeType].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
              </thead>
              <tbody>
                {data.map((row, i) => (
                  <tr key={i} className="border-t hover:bg-gray-50">
                    {ROWKEYS[activeType].map(k => (
                      <td key={k} className="px-4 py-3">{row[k] ?? "-"}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
            {data.length === 0 && <p className="text-center text-gray-400 py-8">No data for selected range.</p>}
          </div>
        )}
      </div>
    </div>
  );
}
