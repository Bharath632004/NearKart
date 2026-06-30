import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const INIT = [
  { id: 1, name: 'Rice (5 kg)', price: 250, stock: 100, category: 'Grocery' },
  { id: 2, name: 'Toor Dal (1 kg)', price: 120, stock: 60, category: 'Grocery' },
  { id: 3, name: 'Sunflower Oil (1 L)', price: 180, stock: 40, category: 'Grocery' },
];

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 900, margin: '0 auto', padding: 32 },
  table: { width: '100%', background: '#fff', borderRadius: 12, overflow: 'hidden', boxShadow: '0 2px 10px rgba(0,0,0,.07)', borderCollapse: 'collapse' },
  th: { background: '#1a73e8', color: '#fff', padding: '12px 16px', textAlign: 'left', fontWeight: 600 },
  td: { padding: '12px 16px', borderBottom: '1px solid #f0f0f0' },
  form: { background: '#fff', borderRadius: 12, padding: 24, boxShadow: '0 2px 10px rgba(0,0,0,.07)', marginBottom: 24 },
  input: { padding: '8px 12px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, marginRight: 8 },
  btn: { background: '#1a73e8', color: '#fff', border: 'none', borderRadius: 8, padding: '8px 20px', cursor: 'pointer', fontWeight: 600 },
  delBtn: { background: '#ffebee', color: '#c62828', border: 'none', borderRadius: 6, padding: '4px 12px', cursor: 'pointer' },
};

export default function ManageProducts() {
  const [products, setProducts] = useState(INIT);
  const [form, setForm] = useState({ name: '', price: '', stock: '', category: '' });
  const add = () => {
    if (!form.name || !form.price) return;
    setProducts(p => [...p, { ...form, id: Date.now(), price: +form.price, stock: +form.stock }]);
    setForm({ name: '', price: '', stock: '', category: '' });
  };
  const del = (id) => setProducts(p => p.filter(x => x.id !== id));
  return (
    <div style={s.page}>
      <Navbar role="MERCHANT" />
      <div style={s.container}>
        <h2>🛍️ Manage Products</h2>
        <div style={s.form}>
          <h4 style={{ marginTop: 0 }}>Add New Product</h4>
          {['name','price','stock','category'].map(f => (
            <input key={f} style={s.input} placeholder={f.charAt(0).toUpperCase()+f.slice(1)} value={form[f]} onChange={e => setForm(v => ({ ...v, [f]: e.target.value }))} />
          ))}
          <button style={s.btn} onClick={add}>+ Add</button>
        </div>
        <table style={s.table}>
          <thead><tr>{['Name','Price','Stock','Category','Action'].map(h => <th key={h} style={s.th}>{h}</th>)}</tr></thead>
          <tbody>{products.map(p => (
            <tr key={p.id}>
              <td style={s.td}>{p.name}</td>
              <td style={s.td}>₹{p.price}</td>
              <td style={s.td}>{p.stock}</td>
              <td style={s.td}>{p.category}</td>
              <td style={s.td}><button style={s.delBtn} onClick={() => del(p.id)}>Delete</button></td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </div>
  );
}
