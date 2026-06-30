import React, { useState } from "react";

const initialCoupons = [
  { id: "c1", code: "SAVE10", discount: "10%", expiry: "2025-07-31", active: true },
  { id: "c2", code: "FIRST50", discount: "₹50", expiry: "2025-08-15", active: true },
];

export default function Coupons() {
  const [coupons, setCoupons] = useState(initialCoupons);
  const [form, setForm] = useState({ code: "", discount: "", expiry: "" });

  const addCoupon = (e) => {
    e.preventDefault();
    if (!form.code || !form.discount || !form.expiry) return;
    setCoupons(prev => [...prev, { id: Date.now().toString(), ...form, active: true }]);
    setForm({ code: "", discount: "", expiry: "" });
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Coupons & Offers</h2>
      <div className="bg-white rounded-xl shadow p-6 mb-6">
        <h3 className="font-semibold text-gray-700 mb-4">Add New Coupon</h3>
        <form onSubmit={addCoupon} className="flex flex-wrap gap-3">
          <input className="border rounded-lg px-3 py-2 text-sm flex-1 min-w-32" placeholder="Code (e.g. SAVE20)" value={form.code} onChange={e => setForm({...form, code: e.target.value})} />
          <input className="border rounded-lg px-3 py-2 text-sm flex-1 min-w-32" placeholder="Discount (e.g. 10% or ₹50)" value={form.discount} onChange={e => setForm({...form, discount: e.target.value})} />
          <input type="date" className="border rounded-lg px-3 py-2 text-sm" value={form.expiry} onChange={e => setForm({...form, expiry: e.target.value})} />
          <button type="submit" className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700">Add</button>
        </form>
      </div>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Code", "Discount", "Expiry", "Status"].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
          </thead>
          <tbody>
            {coupons.map(c => (
              <tr key={c.id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-mono font-bold text-indigo-600">{c.code}</td>
                <td className="px-4 py-3">{c.discount}</td>
                <td className="px-4 py-3">{c.expiry}</td>
                <td className="px-4 py-3"><span className="bg-green-100 text-green-700 px-2 py-1 rounded-full text-xs">{c.active ? "Active" : "Expired"}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
