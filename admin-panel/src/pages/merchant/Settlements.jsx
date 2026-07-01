import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockSettlements = [
  { _id: "s1", period: "Jun 1–15, 2026", orders: 42, grossAmount: 18500, commission: 1850, netAmount: 16650, status: "paid", paidOn: "2026-06-18" },
  { _id: "s2", period: "Jun 16–30, 2026", orders: 38, grossAmount: 16200, commission: 1620, netAmount: 14580, status: "pending", paidOn: null },
  { _id: "s3", period: "May 16–31, 2026", orders: 55, grossAmount: 24000, commission: 2400, netAmount: 21600, status: "paid", paidOn: "2026-06-03" },
];

export default function Settlements() {
  const [settlements, setSettlements] = useState(mockSettlements);

  useEffect(() => {
    API.get("/merchant/settlements").then(res => setSettlements(res.data)).catch(() => {});
  }, []);

  const pending = settlements.filter(s => s.status === "pending").reduce((sum, s) => sum + s.netAmount, 0);
  const paid = settlements.filter(s => s.status === "paid").reduce((sum, s) => sum + s.netAmount, 0);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Settlements</h2>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <div className="bg-green-50 border border-green-200 rounded-xl p-5">
          <p className="text-sm text-green-600 font-medium">Total Paid</p>
          <p className="text-2xl font-bold text-green-700">₹{paid.toLocaleString()}</p>
        </div>
        <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-5">
          <p className="text-sm text-yellow-600 font-medium">Pending Settlement</p>
          <p className="text-2xl font-bold text-yellow-700">₹{pending.toLocaleString()}</p>
        </div>
        <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-5">
          <p className="text-sm text-indigo-600 font-medium">Commission Rate</p>
          <p className="text-2xl font-bold text-indigo-700">10%</p>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Period", "Orders", "Gross Amount", "Commission (10%)", "Net Payable", "Status", "Paid On"].map(h =>
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            )}</tr>
          </thead>
          <tbody>
            {settlements.map(s => (
              <tr key={s._id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{s.period}</td>
                <td className="px-4 py-3 text-center">{s.orders}</td>
                <td className="px-4 py-3">₹{s.grossAmount.toLocaleString()}</td>
                <td className="px-4 py-3 text-red-600">- ₹{s.commission.toLocaleString()}</td>
                <td className="px-4 py-3 font-bold text-green-700">₹{s.netAmount.toLocaleString()}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                    s.status === "paid" ? "bg-green-100 text-green-700" : "bg-yellow-100 text-yellow-700"
                  }`}>{s.status}</span>
                </td>
                <td className="px-4 py-3 text-gray-500">{s.paidOn || "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
