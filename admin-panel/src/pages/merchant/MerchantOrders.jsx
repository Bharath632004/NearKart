import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockOrders = [
  { _id: "o1", customer: "Ravi Kumar", items: ["Tomatoes x2", "Milk x1"], amount: 350, status: "pending", time: "10:30 AM", address: "MG Road, Vijayawada" },
  { _id: "o2", customer: "Priya Sharma", items: ["Basmati Rice x1"], amount: 120, status: "confirmed", time: "09:15 AM", address: "Labbipet, Vijayawada" },
  { _id: "o3", customer: "Suresh Babu", items: ["Tomatoes x5", "Milk x3"], amount: 540, status: "delivered", time: "08:50 AM", address: "Governorpet, Vijayawada" },
];

const STATUS_COLORS = {
  pending: "bg-yellow-100 text-yellow-700",
  confirmed: "bg-blue-100 text-blue-700",
  out_for_delivery: "bg-indigo-100 text-indigo-700",
  delivered: "bg-green-100 text-green-700",
  cancelled: "bg-red-100 text-red-700",
};

export default function MerchantOrders() {
  const [orders, setOrders] = useState(mockOrders);
  const [filter, setFilter] = useState("all");

  useEffect(() => {
    API.get("/merchant/orders").then(res => setOrders(res.data)).catch(() => {});
  }, []);

  const updateStatus = async (id, status) => {
    try { await API.patch(`/merchant/orders/${id}/status`, { status }); } catch {}
    setOrders(prev => prev.map(o => o._id === id ? { ...o, status } : o));
  };

  const filtered = filter === "all" ? orders : orders.filter(o => o.status === filter);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">My Orders</h2>

      <div className="flex gap-2 mb-5 flex-wrap">
        {["all", "pending", "confirmed", "out_for_delivery", "delivered", "cancelled"].map(s => (
          <button key={s} onClick={() => setFilter(s)}
            className={`px-3 py-1.5 rounded-full text-xs font-medium transition ${
              filter === s ? "bg-indigo-600 text-white" : "bg-white border border-gray-300 text-gray-600 hover:border-indigo-400"
            }`}>
            {s.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase())}
          </button>
        ))}
      </div>

      <div className="space-y-4">
        {filtered.map(o => (
          <div key={o._id} className="bg-white rounded-xl shadow p-5 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-1">
                <p className="font-bold text-gray-800">{o.customer}</p>
                <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${STATUS_COLORS[o.status]}`}>
                  {o.status.replace(/_/g, " ")}
                </span>
              </div>
              <p className="text-xs text-gray-500 mb-1">{o.time} · {o.address}</p>
              <p className="text-sm text-gray-600">{o.items.join(", ")}</p>
            </div>
            <div className="text-right flex flex-col items-end gap-2">
              <p className="text-xl font-bold text-indigo-700">₹{o.amount}</p>
              {o.status !== "delivered" && o.status !== "cancelled" && (
                <select className="border rounded px-2 py-1 text-xs"
                  value={o.status} onChange={e => updateStatus(o._id, e.target.value)}>
                  {["pending", "confirmed", "out_for_delivery", "delivered", "cancelled"].map(s =>
                    <option key={s} value={s}>{s.replace(/_/g, " ")}</option>
                  )}
                </select>
              )}
            </div>
          </div>
        ))}
        {filtered.length === 0 && (
          <div className="bg-white rounded-xl shadow p-10 text-center text-gray-400">No orders found.</div>
        )}
      </div>
    </div>
  );
}
