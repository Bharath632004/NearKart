import React from 'react';
import Navbar from '../../components/Navbar';

const ORDERS = [
  { id: 'NK1001', shop: 'Sri Lakshmi Grocery', date: '2026-06-28', total: 620, status: 'Delivered', statusColor: '#2e7d32' },
  { id: 'NK1002', shop: 'Fresh Vegetables & Fruits', date: '2026-06-25', total: 185, status: 'Delivered', statusColor: '#2e7d32' },
  { id: 'NK1003', shop: 'Annapurna Bakery', date: '2026-06-30', total: 240, status: 'Out for Delivery', statusColor: '#e65100' },
];

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 750, margin: '0 auto', padding: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 20, marginBottom: 16, boxShadow: '0 2px 10px rgba(0,0,0,.07)' },
  row: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  id: { fontWeight: 700, color: '#1a73e8' },
  shop: { color: '#333', fontWeight: 600, margin: '6px 0' },
  meta: { color: '#888', fontSize: 13 },
  badge: { borderRadius: 20, padding: '4px 14px', fontWeight: 600, fontSize: 13 },
};

export default function OrderHistory() {
  return (
    <div style={s.page}>
      <Navbar role="CUSTOMER" />
      <div style={s.container}>
        <h2>📦 My Orders</h2>
        {ORDERS.map(o => (
          <div key={o.id} style={s.card}>
            <div style={s.row}>
              <span style={s.id}>#{o.id}</span>
              <span style={{ ...s.badge, background: o.statusColor + '22', color: o.statusColor }}>{o.status}</span>
            </div>
            <div style={s.shop}>{o.shop}</div>
            <div style={{ ...s.row, marginTop: 8 }}>
              <span style={s.meta}>{o.date}</span>
              <span style={{ fontWeight: 700 }}>₹{o.total}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
