import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockOrders = [
  { _id: "o1", customer: "Ravi", merchant: "Fresh Veggies", amount: 320, status: "delivered", date: "2025-06-28" },
  { _id: "o2", customer: "Priya", merchant: "Daily Dairy", amount: 150, status: "pending", date: "2025-06-29" },
  { _id: "o3", customer: "Anjali", merchant: "Ravi Kirana", amount: 540, status: "cancelled", date: "2025-06-29" },
];

const statusColor = {
  delivered: "bg-green-100 text-green-700",
  pending: "bg-yellow-100 text-yellow-700",
  cancelled: "bg-red-100 text-red-700",
  processing: "bg-blue-100 text-blue-700",
  shipped: "bg-purple-100 text-purple-700",
};

export default function Orders() {
  const [orders, setOrders] = useState(mockOrders);

  useEffect(() => {
    API.get("/admin/orders").then(res => setOrders(res.data)).catch(() => {});
  }, []);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Orders Overview</h2>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Order ID", "Customer", "Merchant", "Amount", "Date", "Status"].map(h => (
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            ))}</tr>
          </thead>
          <tbody>
            {orders.map(o => (
              <tr key={o._id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-mono text-xs">{o._id}</td>
                <td className="px-4 py-3">{o.customer}</td>
                <td className="px-4 py-3">{o.merchant}</td>
                <td className="px-4 py-3 font-semibold">₹{o.amount}</td>
                <td className="px-4 py-3">{o.date}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${statusColor[o.status] || "bg-gray-100 text-gray-700"}`}>
                    {o.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
