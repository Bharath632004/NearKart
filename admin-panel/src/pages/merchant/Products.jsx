import React, { useState } from "react";

const initialProducts = [
  { id: "p1", name: "Tomatoes", category: "Vegetables", price: 30, stock: 50, unit: "kg" },
  { id: "p2", name: "Milk 500ml", category: "Dairy", price: 25, stock: 100, unit: "packet" },
  { id: "p3", name: "Bread", category: "Bakery", price: 35, stock: 30, unit: "loaf" },
];

export default function Products() {
  const [products, setProducts] = useState(initialProducts);
  const [form, setForm] = useState({ name: "", category: "", price: "", stock: "", unit: "" });
  const [showForm, setShowForm] = useState(false);

  const addProduct = (e) => {
    e.preventDefault();
    setProducts(prev => [...prev, { id: Date.now().toString(), ...form, price: +form.price, stock: +form.stock }]);
    setForm({ name: "", category: "", price: "", stock: "", unit: "" });
    setShowForm(false);
  };

  const deleteProduct = (id) => setProducts(prev => prev.filter(p => p.id !== id));

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Product & Inventory</h2>
        <button onClick={() => setShowForm(!showForm)} className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700">+ Add Product</button>
      </div>
      {showForm && (
        <div className="bg-white rounded-xl shadow p-6 mb-6">
          <form onSubmit={addProduct} className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            {[
              ["name", "Product Name", "text"],
              ["category", "Category", "text"],
              ["price", "Price (₹)", "number"],
              ["stock", "Stock Qty", "number"],
              ["unit", "Unit (kg/pkt/etc)", "text"],
            ].map(([key, placeholder, type]) => (
              <input key={key} type={type} placeholder={placeholder} required
                className="border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500"
                value={form[key]} onChange={e => setForm({...form, [key]: e.target.value})}
              />
            ))}
            <button type="submit" className="bg-green-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-green-700">Save</button>
          </form>
        </div>
      )}
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
            <tr>{["Product", "Category", "Price", "Stock", "Unit", "Action"].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
          </thead>
          <tbody>
            {products.map(p => (
              <tr key={p.id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{p.name}</td>
                <td className="px-4 py-3">{p.category}</td>
                <td className="px-4 py-3 font-semibold">₹{p.price}</td>
                <td className="px-4 py-3">
                  <span className={`font-semibold ${p.stock < 10 ? "text-red-600" : "text-green-600"}`}>{p.stock}</span>
                </td>
                <td className="px-4 py-3">{p.unit}</td>
                <td className="px-4 py-3">
                  <button onClick={() => deleteProduct(p.id)} className="text-red-500 hover:text-red-700 text-xs">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
