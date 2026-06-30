import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockPartners = [
  { _id: "d1", name: "Kiran Babu", phone: "9876501234", zone: "Vijayawada", status: "active", deliveries: 120 },
  { _id: "d2", name: "Sai Teja", phone: "9988001122", zone: "Guntur", status: "inactive", deliveries: 85 },
];

export default function DeliveryPartners() {
  const [partners, setPartners] = useState(mockPartners);
  useEffect(() => {
    API.get("/admin/delivery-partners").then(res => setPartners(res.data)).catch(() => {});
  }, []);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Delivery Partner Management</h2>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Name", "Phone", "Zone", "Total Deliveries", "Status"].map(h =>
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            )}</tr>
          </thead>
          <tbody>
            {partners.map(p => (
              <tr key={p._id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{p.name}</td>
                <td className="px-4 py-3">{p.phone}</td>
                <td className="px-4 py-3">{p.zone}</td>
                <td className="px-4 py-3 text-center font-semibold">{p.deliveries}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${p.status === "active" ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"}`}>
                    {p.status}
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
