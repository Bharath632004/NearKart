import React, { useState } from "react";

const mockOrders = [
  { id: "o1", customer: "Ravi Kumar", items: "2x Tomatoes, 1x Milk", amount: 85, status: "pending", time: "10:30 AM" },
  { id: "o2", customer: "Anjali Reddy", items: "1x Bread, 3x Milk", amount: 110, status: "accepted", time: "11:00 AM" },
  { id: "o3", customer: "Sita Ram", items: "5kg Tomatoes", amount: 150, status: "delivered", time: "09:15 AM" },
];

const nextStatus = { pending: "accepted", accepted: "dispatched", dispatched: "delivered" };
const statusColor = {
  pending: "bg-yellow-100 text-yellow-700",
  accepted: "bg-blue-100 text-blue-700",
  dispatched: "bg-purple-100 text-purple-700",
  delivered: "bg-green-100 text-green-700"
};

export default function MerchantOrders() {
  const [orders, setOrders] = useState(mockOrders);

  const updateStatus = (id) => {
    setOrders(prev => prev.map(o =>
      o.id === id && nextStatus[o.status] ? { ...o, status: nextStatus[o.status] } : o
    ));
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Order Acceptance</h2>
      <div className="grid gap-4">
        {orders.map(o => (
          <div key={o.id} className="bg-white rounded-xl shadow p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <p className="font-semibold text-gray-800">{o.customer}</p>
              <p className="text-sm text-gray-500">{o.items}</p>
              <p className="text-xs text-gray-400">{o.time}</p>
            </div>
            <div className="flex items-center gap-4">
              <span className="font-bold text-gray-700">₹{o.amount}</span>
              <span className={`px-3 py-1 rounded-full text-xs font-semibold ${statusColor[o.status]}`}>{o.status}</span>
              {nextStatus[o.status] && (
                <button onClick={() => updateStatus(o.id)} className="bg-indigo-600 text-white px-3 py-1 rounded-lg text-xs hover:bg-indigo-700">
                  Mark as {nextStatus[o.status]}
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
