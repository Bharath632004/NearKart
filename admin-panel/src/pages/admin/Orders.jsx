import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockOrders = [
  { _id: "o1", customer: "Ravi Kumar", merchant: "Fresh Veggies", amount: 350, status: "pending", date: "2026-06-28" },
  { _id: "o2", customer: "Priya Sharma", merchant: "Daily Dairy", amount: 120, status: "delivered", date: "2026-06-27" },
  { _id: "o3", customer: "Anjali Reddy", merchant: "Ravi Kirana", amount: 890, status: "cancelled", date: "2026-06-26" },
  { _id: "o4", customer: "Suresh Babu", merchant: "Fresh Veggies", amount: 540, status: "out_for_delivery", date: "2026-06-28" },
];

const STATUS_COLORS = {
  pending: "bg-yellow-100 text-yellow-700",
  confirmed: "bg-blue-100 text-blue-700",
  out_for_delivery: "bg-indigo-100 text-indigo-700",
  delivered: "bg-green-100 text-green-700",
  cancelled: "bg-red-100 text-red-700",
};

export default function Orders() {
  const [orders, setOrders] = useState(mockOrders);
  const [filter, setFilter] = useState("all");
  const [search, setSearch] = useState("");

  useEffect(() => {
    API.get("/admin/orders").then(res => setOrders(res.data)).catch(() => {});
  }, []);

  const filtered = orders.filter(o => {
    const matchFilter = filter === "all" || o.status === filter;
    const matchSearch = o.customer.toLowerCase().includes(search.toLowerCase()) ||
      o.merchant.toLowerCase().includes(search.toLowerCase());
    return matchFilter && matchSearch;
  });

  const updateStatus = async (id, status) => {
    try { await API.patch(`/admin/orders/${id}/status`, { status }); } catch {}
    setOrders(prev => prev.map(o => o._id === id ? { ...o, status } : o));
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Order Management</h2>
      <div className="bg-white rounded-xl shadow p-5">
        <div className="flex flex-wrap gap-3 mb-4">
          <input className="border border-gray-300 rounded-lg px-4 py-2 w-full sm:w-64"
            placeholder="Search customer or merchant..." value={search}
            onChange={e => setSearch(e.target.value)} />
          <select className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
            value={filter} onChange={e => setFilter(e.target.value)}>
            {["all", "pending", "confirmed", "out_for_delivery", "delivered", "cancelled"].map(s => (
              <option key={s} value={s}>{s.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase())}</option>
            ))}
          </select>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
              <tr>{["Order ID", "Customer", "Merchant", "Amount", "Date", "Status", "Update"].map(h =>
                <th key={h} className="px-4 py-3 text-left">{h}</th>
              )}</tr>
            </thead>
            <tbody>
              {filtered.map(o => (
                <tr key={o._id} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs text-gray-500">#{o._id}</td>
                  <td className="px-4 py-3 font-medium">{o.customer}</td>
                  <td className="px-4 py-3">{o.merchant}</td>
                  <td className="px-4 py-3 font-semibold">₹{o.amount}</td>
                  <td className="px-4 py-3 text-gray-500">{o.date}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${STATUS_COLORS[o.status] || "bg-gray-100 text-gray-600"}`}>
                      {o.status.replace(/_/g, " ")}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <select className="border rounded px-2 py-1 text-xs"
                      value={o.status} onChange={e => updateStatus(o._id, e.target.value)}>
                      {["pending", "confirmed", "out_for_delivery", "delivered", "cancelled"].map(s => (
                        <option key={s} value={s}>{s.replace(/_/g, " ")}</option>
                      ))}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {filtered.length === 0 && <p className="text-center text-gray-400 py-6">No orders found.</p>}
        </div>
      </div>
    </div>
  );
}
