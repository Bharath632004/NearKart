import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const INIT = [
  { id: 'NK1003', customer: 'Suresh Babu', address: '12-3, Gandhi Nagar, Vanikamdinne', phone: '9876500001', shop: 'Sri Lakshmi Grocery', distance: '1.2 km', status: 'Picked Up' },
  { id: 'NK1005', customer: 'Meena Devi', address: '45, Market Street, Vanikamdinne', phone: '9876500002', shop: 'Fresh Vegetables', distance: '0.8 km', status: 'Assigned' },
  { id: 'NK1006', customer: 'Kiran Kumar', address: '7, Temple Road', phone: '9876500003', shop: 'Annapurna Bakery', distance: '2.1 km', status: 'Assigned' },
];

const STATUS_COLORS = { Assigned: '#1565c0', 'Picked Up': '#e65100', Delivered: '#2e7d32' };

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 800, margin: '0 auto', padding: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 20, marginBottom: 14, boxShadow: '0 2px 8px rgba(0,0,0,.07)' },
  badge: { borderRadius: 20, padding: '3px 12px', fontSize: 12, fontWeight: 700 },
  btn: { border: 'none', borderRadius: 8, padding: '8px 20px', cursor: 'pointer', fontWeight: 600, marginRight: 8 },
};

export default function ActiveDeliveries() {
  const [orders, setOrders] = useState(INIT);
  const updateStatus = (id, status) => setOrders(o => o.map(x => x.id === id ? { ...x, status } : x));
  return (
    <div style={s.page}>
      <Navbar role="DELIVERY" />
      <div style={s.container}>
        <h2>📍 Active Deliveries</h2>
        {orders.map(o => (
          <div key={o.id} style={s.card}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
              <span style={{ fontWeight: 700, color: '#1a73e8' }}>#{o.id}</span>
              <span style={{ ...s.badge, background: (STATUS_COLORS[o.status]||'#888')+'22', color: STATUS_COLORS[o.status] }}>{o.status}</span>
            </div>
            <div><strong>Customer:</strong> {o.customer} · {o.phone}</div>
            <div style={{ color: '#666', margin: '4px 0' }}>📍 {o.address}</div>
            <div style={{ color: '#666', fontSize: 13 }}>🏪 {o.shop} · {o.distance}</div>
            <div style={{ marginTop: 12 }}>
              {o.status === 'Assigned' && <button style={{ ...s.btn, background: '#e3f2fd', color: '#1565c0' }} onClick={() => updateStatus(o.id, 'Picked Up')}>Mark Picked Up</button>}
              {o.status === 'Picked Up' && <button style={{ ...s.btn, background: '#e8f5e9', color: '#2e7d32' }} onClick={() => updateStatus(o.id, 'Delivered')}>Mark Delivered ✅</button>}
              {o.status === 'Delivered' && <span style={{ color: '#2e7d32', fontWeight: 600 }}>✅ Completed</span>}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
