import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockCoupons = [
  { _id: "c1", code: "SAVE10", discount: 10, type: "percent", minOrder: 200, expiry: "2026-08-01", active: true },
  { _id: "c2", code: "FLAT50", discount: 50, type: "flat", minOrder: 300, expiry: "2026-07-15", active: true },
  { _id: "c3", code: "WELCOME20", discount: 20, type: "percent", minOrder: 100, expiry: "2026-06-30", active: false },
];

const emptyForm = { code: "", discount: "", type: "percent", minOrder: "", expiry: "" };

export default function Coupons() {
  const [coupons, setCoupons] = useState(mockCoupons);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editId, setEditId] = useState(null);

  useEffect(() => {
    API.get("/admin/coupons").then(res => setCoupons(res.data)).catch(() => {});
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (editId) {
      try { await API.put(`/admin/coupons/${editId}`, form); } catch {}
      setCoupons(prev => prev.map(c => c._id === editId ? { ...c, ...form } : c));
      setEditId(null);
    } else {
      const newCoupon = { ...form, _id: Date.now().toString(), active: true };
      try { await API.post("/admin/coupons", form); } catch {}
      setCoupons(prev => [...prev, newCoupon]);
    }
    setForm(emptyForm);
    setShowForm(false);
  };

  const toggleActive = async (id, current) => {
    try { await API.patch(`/admin/coupons/${id}/toggle`); } catch {}
    setCoupons(prev => prev.map(c => c._id === id ? { ...c, active: !current } : c));
  };

  const deleteCoupon = async (id) => {
    if (!window.confirm("Delete this coupon?")) return;
    try { await API.delete(`/admin/coupons/${id}`); } catch {}
    setCoupons(prev => prev.filter(c => c._id !== id));
  };

  const startEdit = (coupon) => {
    setForm({ code: coupon.code, discount: coupon.discount, type: coupon.type, minOrder: coupon.minOrder, expiry: coupon.expiry });
    setEditId(coupon._id);
    setShowForm(true);
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Coupon Management</h2>
        <button onClick={() => { setShowForm(!showForm); setEditId(null); setForm(emptyForm); }}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700">
          + Add Coupon
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow p-5 mb-6 grid grid-cols-1 sm:grid-cols-3 gap-4">
          <input required placeholder="Coupon Code (e.g. SAVE10)"
            className="border border-gray-300 rounded-lg px-3 py-2 uppercase"
            value={form.code} onChange={e => setForm({ ...form, code: e.target.value.toUpperCase() })} />
          <input required type="number" placeholder="Discount Value"
            className="border border-gray-300 rounded-lg px-3 py-2"
            value={form.discount} onChange={e => setForm({ ...form, discount: e.target.value })} />
          <select className="border border-gray-300 rounded-lg px-3 py-2"
            value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>
            <option value="percent">Percentage (%)</option>
            <option value="flat">Flat (₹)</option>
          </select>
          <input required type="number" placeholder="Min Order Amount"
            className="border border-gray-300 rounded-lg px-3 py-2"
            value={form.minOrder} onChange={e => setForm({ ...form, minOrder: e.target.value })} />
          <input required type="date"
            className="border border-gray-300 rounded-lg px-3 py-2"
            value={form.expiry} onChange={e => setForm({ ...form, expiry: e.target.value })} />
          <button type="submit" className="bg-green-500 text-white rounded-lg px-4 py-2 hover:bg-green-600">
            {editId ? "Update Coupon" : "Create Coupon"}
          </button>
        </form>
      )}

      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Code", "Discount", "Type", "Min Order", "Expiry", "Status", "Actions"].map(h =>
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            )}</tr>
          </thead>
          <tbody>
            {coupons.map(c => (
              <tr key={c._id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-mono font-bold text-indigo-700">{c.code}</td>
                <td className="px-4 py-3 font-semibold">{c.type === "percent" ? `${c.discount}%` : `₹${c.discount}`}</td>
                <td className="px-4 py-3 capitalize">{c.type}</td>
                <td className="px-4 py-3">₹{c.minOrder}</td>
                <td className="px-4 py-3 text-gray-500">{c.expiry}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${c.active ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"}`}>
                    {c.active ? "Active" : "Inactive"}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    <button onClick={() => startEdit(c)} className="bg-yellow-400 hover:bg-yellow-500 text-white px-2 py-1 rounded text-xs">Edit</button>
                    <button onClick={() => toggleActive(c._id, c.active)} className={`px-2 py-1 rounded text-xs text-white ${c.active ? "bg-gray-400 hover:bg-gray-500" : "bg-green-500 hover:bg-green-600"}`}>
                      {c.active ? "Disable" : "Enable"}
                    </button>
                    <button onClick={() => deleteCoupon(c._id)} className="bg-red-500 hover:bg-red-600 text-white px-2 py-1 rounded text-xs">Delete</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
