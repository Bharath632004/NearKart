import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockPayments = [
  { _id: "p1", orderId: "o1", customer: "Ravi Kumar", amount: 350, method: "UPI", status: "success", date: "2026-06-28" },
  { _id: "p2", orderId: "o2", customer: "Priya Sharma", amount: 120, method: "COD", status: "success", date: "2026-06-27" },
  { _id: "p3", orderId: "o3", customer: "Anjali Reddy", amount: 890, method: "Card", status: "refunded", date: "2026-06-26" },
  { _id: "p4", orderId: "o4", customer: "Suresh Babu", amount: 540, method: "UPI", status: "pending", date: "2026-06-28" },
];

const STATUS_COLORS = {
  success: "bg-green-100 text-green-700",
  pending: "bg-yellow-100 text-yellow-700",
  failed: "bg-red-100 text-red-700",
  refunded: "bg-purple-100 text-purple-700",
};

export default function Payments() {
  const [payments, setPayments] = useState(mockPayments);
  const [filter, setFilter] = useState("all");
  const [search, setSearch] = useState("");

  useEffect(() => {
    API.get("/admin/payments").then(res => setPayments(res.data)).catch(() => {});
  }, []);

  const filtered = payments.filter(p => {
    const matchFilter = filter === "all" || p.status === filter;
    const matchSearch = p.customer.toLowerCase().includes(search.toLowerCase());
    return matchFilter && matchSearch;
  });

  const total = filtered.reduce((sum, p) => p.status === "success" ? sum + p.amount : sum, 0);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Payment Management</h2>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
        {[
          { label: "Total Revenue", value: `₹${payments.filter(p=>p.status==="success").reduce((s,p)=>s+p.amount,0)}`, color:"bg-green-50 border-green-200 text-green-700" },
          { label: "Pending", value: payments.filter(p=>p.status==="pending").length, color:"bg-yellow-50 border-yellow-200 text-yellow-700" },
          { label: "Refunded", value: payments.filter(p=>p.status==="refunded").length, color:"bg-purple-50 border-purple-200 text-purple-700" },
          { label: "Filtered Total", value: `₹${total}`, color:"bg-indigo-50 border-indigo-200 text-indigo-700" },
        ].map(c => (
          <div key={c.label} className={`rounded-xl border p-4 ${c.color}`}>
            <p className="text-xs font-medium mb-1">{c.label}</p>
            <p className="text-xl font-bold">{c.value}</p>
          </div>
        ))}
      </div>
      <div className="bg-white rounded-xl shadow p-5">
        <div className="flex flex-wrap gap-3 mb-4">
          <input className="border border-gray-300 rounded-lg px-4 py-2 w-full sm:w-64"
            placeholder="Search customer..." value={search}
            onChange={e => setSearch(e.target.value)} />
          <select className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
            value={filter} onChange={e => setFilter(e.target.value)}>
            {["all", "success", "pending", "failed", "refunded"].map(s => (
              <option key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</option>
            ))}
          </select>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
              <tr>{["Payment ID", "Order", "Customer", "Amount", "Method", "Date", "Status"].map(h =>
                <th key={h} className="px-4 py-3 text-left">{h}</th>
              )}</tr>
            </thead>
            <tbody>
              {filtered.map(p => (
                <tr key={p._id} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs text-gray-400">#{p._id}</td>
                  <td className="px-4 py-3 text-xs">#{p.orderId}</td>
                  <td className="px-4 py-3 font-medium">{p.customer}</td>
                  <td className="px-4 py-3 font-semibold">₹{p.amount}</td>
                  <td className="px-4 py-3">{p.method}</td>
                  <td className="px-4 py-3 text-gray-500">{p.date}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${STATUS_COLORS[p.status]}`}>
                      {p.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {filtered.length === 0 && <p className="text-center text-gray-400 py-6">No payments found.</p>}
        </div>
      </div>
    </div>
  );
}
