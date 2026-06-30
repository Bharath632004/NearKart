import React from "react";

const settlements = [
  { id: "s1", period: "Jun 1–7", orders: 42, gross: 18500, fee: 925, net: 17575, status: "paid" },
  { id: "s2", period: "Jun 8–14", orders: 61, gross: 26200, fee: 1310, net: 24890, status: "paid" },
  { id: "s3", period: "Jun 15–21", orders: 53, gross: 21800, fee: 1090, net: 20710, status: "processing" },
  { id: "s4", period: "Jun 22–28", orders: 74, gross: 31200, fee: 1560, net: 29640, status: "pending" },
];

const statusColor = {
  paid: "bg-green-100 text-green-700",
  processing: "bg-yellow-100 text-yellow-700",
  pending: "bg-gray-100 text-gray-600"
};

export default function Settlements() {
  const total = settlements.filter(s => s.status === "paid").reduce((a, s) => a + s.net, 0);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Settlements</h2>
      <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-5 mb-6 flex items-center justify-between">
        <span className="text-gray-700 font-medium">Total Settled Amount</span>
        <span className="text-2xl font-bold text-indigo-700">₹{total.toLocaleString()}</span>
      </div>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Period", "Orders", "Gross (₹)", "Platform Fee", "Net Payout", "Status"].map(h =>
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            )}</tr>
          </thead>
          <tbody>
            {settlements.map(s => (
              <tr key={s.id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{s.period}</td>
                <td className="px-4 py-3">{s.orders}</td>
                <td className="px-4 py-3">₹{s.gross.toLocaleString()}</td>
                <td className="px-4 py-3 text-red-600">-₹{s.fee}</td>
                <td className="px-4 py-3 font-bold text-green-700">₹{s.net.toLocaleString()}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${statusColor[s.status]}`}>{s.status}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
