import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockPayments = [
  { _id: "p1", user: "Ravi Kumar", amount: 320, method: "UPI", status: "success", date: "2025-06-28" },
  { _id: "p2", user: "Priya Sharma", amount: 150, method: "Card", status: "success", date: "2025-06-29" },
  { _id: "p3", user: "Anjali Reddy", amount: 540, method: "COD", status: "refunded", date: "2025-06-29" },
];

export default function Payments() {
  const [payments, setPayments] = useState(mockPayments);

  useEffect(() => {
    API.get("/admin/payments").then(res => setPayments(res.data)).catch(() => {});
  }, []);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Payments & Refunds</h2>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["ID", "User", "Amount", "Method", "Status", "Date"].map(h => (
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            ))}</tr>
          </thead>
          <tbody>
            {payments.map(p => (
              <tr key={p._id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-mono text-xs">{p._id}</td>
                <td className="px-4 py-3">{p.user}</td>
                <td className="px-4 py-3 font-semibold">₹{p.amount}</td>
                <td className="px-4 py-3">{p.method}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                    p.status === "success" ? "bg-green-100 text-green-700" : "bg-orange-100 text-orange-700"
                  }`}>{p.status}</span>
                </td>
                <td className="px-4 py-3">{p.date}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
