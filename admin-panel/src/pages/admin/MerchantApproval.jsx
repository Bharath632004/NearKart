import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockMerchants = [
  { _id: "m1", shopName: "Fresh Veggies", owner: "Suresh", email: "suresh@shop.com", status: "pending" },
  { _id: "m2", shopName: "Daily Dairy", owner: "Lakshmi", email: "lakshmi@dairy.com", status: "approved" },
  { _id: "m3", shopName: "Ravi Kirana", owner: "Ravi", email: "ravi@kirana.com", status: "pending" },
];

export default function MerchantApproval() {
  const [merchants, setMerchants] = useState(mockMerchants);

  useEffect(() => {
    API.get("/admin/merchants").then(res => setMerchants(res.data)).catch(() => {});
  }, []);

  const updateStatus = async (id, status) => {
    try { await API.patch(`/admin/merchants/${id}/status`, { status }); } catch {}
    setMerchants(prev => prev.map(m => m._id === id ? { ...m, status } : m));
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Merchant Approval</h2>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Shop Name", "Owner", "Email", "Status", "Actions"].map(h => (
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            ))}</tr>
          </thead>
          <tbody>
            {merchants.map(m => (
              <tr key={m._id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{m.shopName}</td>
                <td className="px-4 py-3">{m.owner}</td>
                <td className="px-4 py-3">{m.email}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                    m.status === "approved" ? "bg-green-100 text-green-700" :
                    m.status === "rejected" ? "bg-red-100 text-red-700" :
                    "bg-yellow-100 text-yellow-700"
                  }`}>{m.status}</span>
                </td>
                <td className="px-4 py-3 flex gap-2">
                  {m.status === "pending" && (
                    <>
                      <button onClick={() => updateStatus(m._id, "approved")} className="bg-green-500 hover:bg-green-600 text-white px-3 py-1 rounded text-xs">Approve</button>
                      <button onClick={() => updateStatus(m._id, "rejected")} className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-xs">Reject</button>
                    </>
                  )}
                  {m.status !== "pending" && <span className="text-gray-400 text-xs">No action</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
