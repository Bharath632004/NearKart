import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getMerchantProductsApi, createProductApi, updateProductApi, deleteProductApi } from '../../api/productApi';

const empty = { name: '', description: '', price: '', stock: '', category: '' };

export default function ManageProducts() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(empty);
  const [editing, setEditing] = useState(null);
  const [saving, setSaving] = useState(false);

  const load = () => getMerchantProductsApi().then(r => setProducts(r.data || []))
    .catch(() => setError('Failed to load')).finally(() => setLoading(false));

  useEffect(() => { load(); }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault(); setSaving(true); setError('');
    try {
      if (editing) await updateProductApi(editing, form);
      else await createProductApi(form);
      setShowForm(false); setForm(empty); setEditing(null); load();
    } catch { setError('Failed to save product'); }
    finally { setSaving(false); }
  };

  const handleEdit = (p) => { setForm({ name: p.name, description: p.description, price: p.price, stock: p.stock, category: p.category }); setEditing(p.id); setShowForm(true); };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this product?')) return;
    try { await deleteProductApi(id); load(); } catch { setError('Delete failed'); }
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <div style={styles.header}>
          <h2 style={styles.heading}>📦 Manage Products</h2>
          <button style={styles.addBtn} onClick={() => { setForm(empty); setEditing(null); setShowForm(true); }}>+ Add Product</button>
        </div>
        <ErrorMsg msg={error} />
        {showForm && (
          <div style={styles.formCard}>
            <h3 style={{ marginBottom: 16 }}>{editing ? 'Edit Product' : 'Add New Product'}</h3>
            <form onSubmit={handleSubmit} style={styles.formGrid}>
              {[['name','Product Name'],['category','Category'],['price','Price (₹)'],['stock','Stock Qty'],['description','Description']].map(([n,l]) => (
                <div key={n} style={{ gridColumn: n === 'description' ? 'span 2' : 'auto' }}>
                  <label style={styles.label}>{l}</label>
                  <input name={n} value={form[n]} onChange={handleChange} style={styles.input} required />
                </div>
              ))}
              <div style={{ display: 'flex', gap: 10, gridColumn: 'span 2' }}>
                <button type="submit" style={styles.saveBtn} disabled={saving}>{saving ? 'Saving...' : editing ? 'Update' : 'Create'}</button>
                <button type="button" style={styles.cancelBtn} onClick={() => { setShowForm(false); setEditing(null); }}>Cancel</button>
              </div>
            </form>
          </div>
        )}
        {loading ? <Loader /> : products.length === 0 ? <p style={{ color: '#888' }}>No products yet.</p> : (
          <table style={styles.table}>
            <thead><tr style={styles.th}>{['Name','Category','Price','Stock','Actions'].map(h => <th key={h} style={styles.thCell}>{h}</th>)}</tr></thead>
            <tbody>
              {products.map(p => (
                <tr key={p.id} style={styles.tr}>
                  <td style={styles.td}>{p.name}</td>
                  <td style={styles.td}>{p.category}</td>
                  <td style={styles.td}>₹{p.price}</td>
                  <td style={styles.td}>{p.stock}</td>
                  <td style={styles.td}>
                    <button style={styles.editBtn} onClick={() => handleEdit(p)}>✏️ Edit</button>
                    <button style={styles.delBtn} onClick={() => handleDelete(p.id)}>🗑 Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 },
  heading: { color: '#1a1a2e' },
  addBtn: { background: '#e94560', color: '#fff', border: 'none', borderRadius: 8, padding: '10px 18px', cursor: 'pointer', fontWeight: 600 },
  formCard: { background: '#fff', borderRadius: 12, padding: 24, marginBottom: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' },
  formGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 },
  label: { display: 'block', fontSize: 13, color: '#555', marginBottom: 4 },
  input: { width: '100%', padding: '8px 12px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, boxSizing: 'border-box' },
  saveBtn: { padding: '10px 24px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600 },
  cancelBtn: { padding: '10px 24px', background: '#eee', color: '#333', border: 'none', borderRadius: 8, cursor: 'pointer' },
  table: { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 10, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  th: { background: '#1a1a2e', color: '#fff' },
  thCell: { padding: '10px 14px', textAlign: 'left', fontSize: 13 },
  tr: { borderBottom: '1px solid #f0f0f0' },
  td: { padding: '10px 14px', fontSize: 14 },
  editBtn: { background: '#3b82f6', color: '#fff', border: 'none', borderRadius: 6, padding: '4px 10px', cursor: 'pointer', marginRight: 6, fontSize: 12 },
  delBtn: { background: '#ef4444', color: '#fff', border: 'none', borderRadius: 6, padding: '4px 10px', cursor: 'pointer', fontSize: 12 },
};
