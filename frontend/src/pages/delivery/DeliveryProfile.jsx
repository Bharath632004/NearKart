import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 500, margin: '0 auto', padding: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 32, boxShadow: '0 2px 10px rgba(0,0,0,.07)' },
  label: { fontWeight: 600, color: '#555', marginBottom: 4, display: 'block' },
  input: { width: '100%', padding: '10px 12px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, boxSizing: 'border-box', marginBottom: 14 },
  btn: { background: '#1a73e8', color: '#fff', border: 'none', borderRadius: 8, padding: '12px 32px', cursor: 'pointer', fontWeight: 700, width: '100%' },
  statusRow: { display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 },
  toggle: { padding: '6px 18px', border: 'none', borderRadius: 20, cursor: 'pointer', fontWeight: 600 },
};

export default function DeliveryProfile() {
  const [form, setForm] = useState({ name: 'Ramesh Delivery', phone: '9876543211', vehicle: 'Bike - TS09AB1234', area: 'Vanikamdinne, 3km radius' });
  const [online, setOnline] = useState(true);
  const [saved, setSaved] = useState(false);
  const handleChange = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }));
  return (
    <div style={s.page}>
      <Navbar role="DELIVERY" />
      <div style={s.container}>
        <h2>👤 Delivery Profile</h2>
        <div style={s.card}>
          <div style={s.statusRow}>
            <span style={{ fontWeight: 600 }}>Status:</span>
            <button style={{ ...s.toggle, background: online ? '#e8f5e9' : '#ffebee', color: online ? '#2e7d32' : '#c62828' }} onClick={() => setOnline(o => !o)}>
              {online ? '🟢 Online' : '🔴 Offline'}
            </button>
          </div>
          {Object.keys(form).map(field => (
            <div key={field}>
              <label style={s.label}>{field.charAt(0).toUpperCase()+field.slice(1)}</label>
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
