import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockPartners = [
  { _id: "d1", name: "Kiran Babu", phone: "9876501234", zone: "Vijayawada", status: "active", deliveries: 120 },
  { _id: "d2", name: "Sai Teja", phone: "9988001122", zone: "Guntur", status: "inactive", deliveries: 85 },
  { _id: "d3", name: "Arjun Rao", phone: "9000112233", zone: "Hyderabad", status: "active", deliveries: 200 },
];

export default function DeliveryPartners() {
  const [partners, setPartners] = useState(mockPartners);
  const [search, setSearch] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: "", phone: "", zone: "" });

  useEffect(() => {
    API.get("/admin/delivery-partners").then(res => setPartners(res.data)).catch(() => {});
  }, []);

  const toggleStatus = async (id, current) => {
    const next = current === "active" ? "inactive" : "active";
    try { await API.patch(`/admin/delivery-partners/${id}/status`, { status: next }); } catch {}
    setPartners(prev => prev.map(p => p._id === id ? { ...p, status: next } : p));
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    const newPartner = { ...form, _id: Date.now().toString(), status: "active", deliveries: 0 };
    try { await API.post("/admin/delivery-partners", form); } catch {}
    setPartners(prev => [...prev, newPartner]);
    setForm({ name: "", phone: "", zone: "" });
    setShowForm(false);
  };

  const filtered = partners.filter(p =>
    p.name.toLowerCase().includes(search.toLowerCase()) ||
    p.zone.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Delivery Partners</h2>
        <button onClick={() => setShowForm(!showForm)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700">
          + Add Partner
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleAdd} className="bg-white rounded-xl shadow p-5 mb-6 grid grid-cols-1 sm:grid-cols-3 gap-4">
          {["name", "phone", "zone"].map(field => (
            <input key={field} required placeholder={field.charAt(0).toUpperCase() + field.slice(1)}
              className="border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-indigo-500"
              value={form[field]} onChange={e => setForm({ ...form, [field]: e.target.value })} />
          ))}
          <button type="submit" className="bg-green-500 text-white rounded-lg px-4 py-2 hover:bg-green-600 col-span-full sm:col-span-1">
            Save
          </button>
        </form>
      )}

      <div className="bg-white rounded-xl shadow p-4">
        <input className="border border-gray-300 rounded-lg px-4 py-2 mb-4 w-full sm:w-80"
          placeholder="Search by name or zone..." value={search}
          onChange={e => setSearch(e.target.value)} />
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
              <tr>{["Name", "Phone", "Zone", "Deliveries", "Status", "Action"].map(h =>
                <th key={h} className="px-4 py-3 text-left">{h}</th>
              )}</tr>
            </thead>
            <tbody>
              {filtered.map(p => (
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
                  <td className="px-4 py-3">
                    <button onClick={() => toggleStatus(p._id, p.status)}
                      className={`px-3 py-1 rounded text-xs text-white ${p.status === "active" ? "bg-red-500 hover:bg-red-600" : "bg-green-500 hover:bg-green-600"}`}>
                      {p.status === "active" ? "Deactivate" : "Activate"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {filtered.length === 0 && <p className="text-center text-gray-400 py-6">No partners found.</p>}
        </div>
      </div>
    </div>
  );
}
