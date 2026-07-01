import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockProducts = [
  { _id: "p1", name: "Tomatoes", category: "Vegetables", price: 30, stock: 50, unit: "kg", active: true },
  { _id: "p2", name: "Milk 500ml", category: "Dairy", price: 25, stock: 100, unit: "packet", active: true },
  { _id: "p3", name: "Basmati Rice", category: "Groceries", price: 120, stock: 30, unit: "kg", active: false },
];

const emptyForm = { name: "", category: "", price: "", stock: "", unit: "kg" };

export default function Products() {
  const [products, setProducts] = useState(mockProducts);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editId, setEditId] = useState(null);
  const [search, setSearch] = useState("");

  useEffect(() => {
    API.get("/merchant/products").then(res => setProducts(res.data)).catch(() => {});
  }, []);

  const openAdd = () => { setForm(emptyForm); setEditId(null); setShowModal(true); };
  const openEdit = (p) => {
    setForm({ name: p.name, category: p.category, price: p.price, stock: p.stock, unit: p.unit });
    setEditId(p._id);
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (editId) {
      try { await API.put(`/merchant/products/${editId}`, form); } catch {}
      setProducts(prev => prev.map(p => p._id === editId ? { ...p, ...form } : p));
    } else {
      const np = { ...form, _id: Date.now().toString(), active: true };
      try { await API.post("/merchant/products", form); } catch {}
      setProducts(prev => [...prev, np]);
    }
    setShowModal(false);
  };

  const deleteProduct = async (id) => {
    if (!window.confirm("Delete this product?")) return;
    try { await API.delete(`/merchant/products/${id}`); } catch {}
    setProducts(prev => prev.filter(p => p._id !== id));
  };

  const toggleActive = async (id, current) => {
    try { await API.patch(`/merchant/products/${id}/toggle`); } catch {}
    setProducts(prev => prev.map(p => p._id === id ? { ...p, active: !current } : p));
  };

  const filtered = products.filter(p =>
    p.name.toLowerCase().includes(search.toLowerCase()) ||
    p.category.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-800">My Products</h2>
        <button onClick={openAdd} className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700">
          + Add Product
        </button>
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md">
            <h3 className="text-lg font-bold mb-4">{editId ? "Edit Product" : "Add New Product"}</h3>
            <form onSubmit={handleSubmit} className="space-y-3">
              {[
                { label: "Product Name", key: "name", type: "text" },
                { label: "Category", key: "category", type: "text" },
                { label: "Price (₹)", key: "price", type: "number" },
                { label: "Stock Quantity", key: "stock", type: "number" },
              ].map(({ label, key, type }) => (
                <div key={key}>
                  <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
                  <input required type={type}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-indigo-500"
                    value={form[key]} onChange={e => setForm({ ...form, [key]: e.target.value })} />
                </div>
              ))}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Unit</label>
                <select className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  value={form.unit} onChange={e => setForm({ ...form, unit: e.target.value })}>
                  {["kg", "gram", "litre", "ml", "packet", "piece", "dozen"].map(u =>
                    <option key={u} value={u}>{u}</option>
                  )}
                </select>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="submit" className="flex-1 bg-indigo-600 text-white py-2 rounded-lg hover:bg-indigo-700 font-semibold">
                  {editId ? "Update" : "Add Product"}
                </button>
                <button type="button" onClick={() => setShowModal(false)} className="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg hover:bg-gray-300">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="bg-white rounded-xl shadow p-4">
        <input className="border border-gray-300 rounded-lg px-4 py-2 mb-4 w-full sm:w-80"
          placeholder="Search products..." value={search} onChange={e => setSearch(e.target.value)} />
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
              <tr>{["Product", "Category", "Price", "Stock", "Unit", "Status", "Actions"].map(h =>
                <th key={h} className="px-4 py-3 text-left">{h}</th>
              )}</tr>
            </thead>
            <tbody>
              {filtered.map(p => (
                <tr key={p._id} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{p.name}</td>
                  <td className="px-4 py-3">{p.category}</td>
                  <td className="px-4 py-3 font-semibold">₹{p.price}</td>
                  <td className="px-4 py-3">{p.stock}</td>
                  <td className="px-4 py-3">{p.unit}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${p.active ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"}`}>
                      {p.active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button onClick={() => openEdit(p)} className="bg-yellow-400 hover:bg-yellow-500 text-white px-2 py-1 rounded text-xs">Edit</button>
                      <button onClick={() => toggleActive(p._id, p.active)} className={`px-2 py-1 rounded text-xs text-white ${p.active ? "bg-gray-400 hover:bg-gray-500" : "bg-green-500 hover:bg-green-600"}`}>
                        {p.active ? "Hide" : "Show"}
                      </button>
                      <button onClick={() => deleteProduct(p._id)} className="bg-red-500 hover:bg-red-600 text-white px-2 py-1 rounded text-xs">Del</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {filtered.length === 0 && <p className="text-center text-gray-400 py-6">No products found.</p>}
        </div>
      </div>
    </div>
  );
}
