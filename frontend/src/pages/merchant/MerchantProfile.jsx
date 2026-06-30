import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 550, margin: '0 auto', padding: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 32, boxShadow: '0 2px 10px rgba(0,0,0,.07)' },
  label: { fontWeight: 600, color: '#555', marginBottom: 4, display: 'block' },
  input: { width: '100%', padding: '10px 12px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, boxSizing: 'border-box', marginBottom: 14 },
  btn: { background: '#1a73e8', color: '#fff', border: 'none', borderRadius: 8, padding: '12px 32px', cursor: 'pointer', fontWeight: 700, width: '100%' },
};

export default function MerchantProfile() {
  const [form, setForm] = useState({ shopName: 'Sri Lakshmi Grocery', ownerName: 'Venkat Rao', phone: '9876543210', address: 'Main Road, Vanikamdinne', category: 'Grocery', gstin: '37AABCU9603R1ZM' });
  const [saved, setSaved] = useState(false);
  const handleChange = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }));
  return (
    <div style={s.page}>
      <Navbar role="MERCHANT" />
      <div style={s.container}>
        <h2>🏪 Shop Profile</h2>
        <div style={s.card}>
          {Object.keys(form).map(field => (
            <div key={field}>
              <label style={s.label}>{field.replace(/([A-Z])/g,' $1').replace(/^./,c=>c.toUpperCase())}</label>
              <input style={s.input} name={field} value={form[field]} onChange={handleChange} />
            </div>
          ))}
          {saved && <p style={{ color: '#2e7d32', textAlign: 'center' }}>✅ Saved!</p>}
          <button style={s.btn} onClick={() => setSaved(true)}>Save Profile</button>
        </div>
      </div>
    </div>
  );
}
