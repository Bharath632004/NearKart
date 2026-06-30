import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 500, margin: '0 auto', padding: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 32, boxShadow: '0 2px 10px rgba(0,0,0,.07)' },
  avatar: { width: 80, height: 80, borderRadius: '50%', background: '#1a73e8', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 32, margin: '0 auto 20px' },
  label: { fontWeight: 600, color: '#555', marginBottom: 4, display: 'block' },
  input: { width: '100%', padding: '10px 12px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, boxSizing: 'border-box', marginBottom: 16 },
  btn: { background: '#1a73e8', color: '#fff', border: 'none', borderRadius: 8, padding: '12px 32px', cursor: 'pointer', fontWeight: 700, width: '100%' },
};

export default function CustomerProfile() {
  const [form, setForm] = useState({ name: 'C Bharath', email: 'bharath@example.com', phone: '9876543210', address: 'Vanikamdinne, Andhra Pradesh' });
  const [saved, setSaved] = useState(false);
  const handleChange = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }));
  return (
    <div style={s.page}>
      <Navbar role="CUSTOMER" />
      <div style={s.container}>
        <h2>👤 My Profile</h2>
        <div style={s.card}>
          <div style={s.avatar}>👤</div>
          {['name','email','phone','address'].map(field => (
            <div key={field}>
              <label style={s.label}>{field.charAt(0).toUpperCase()+field.slice(1)}</label>
              <input style={s.input} name={field} value={form[field]} onChange={handleChange} />
            </div>
          ))}
          {saved && <p style={{ color: '#2e7d32', textAlign: 'center' }}>✅ Profile saved!</p>}
          <button style={s.btn} onClick={() => setSaved(true)}>Save Changes</button>
        </div>
      </div>
    </div>
  );
}
