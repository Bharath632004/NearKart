import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';

const STATS = [
  { label: "Today's Orders", value: 24, icon: '📦', color: '#1a73e8' },
  { label: 'Total Products', value: 48, icon: '🛍️', color: '#7b1fa2' },
  { label: 'Revenue (Today)', value: '₹3,240', icon: '💰', color: '#2e7d32' },
  { label: 'Pending Orders', value: 5, icon: '⏳', color: '#e65100' },
];

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(200px,1fr))', gap: 20, padding: 32, maxWidth: 1000, margin: '0 auto' },
  stat: { background: '#fff', borderRadius: 12, padding: 24, boxShadow: '0 2px 10px rgba(0,0,0,.07)', textAlign: 'center' },
  icon: { fontSize: 36 },
  value: { fontSize: 28, fontWeight: 700, marginTop: 8 },
  label: { color: '#666', fontSize: 13, marginTop: 4 },
  navCard: { background: '#fff', borderRadius: 12, padding: 20, boxShadow: '0 2px 10px rgba(0,0,0,.07)', textDecoration: 'none', color: '#222', textAlign: 'center' },
  h2: { padding: '24px 32px 0', margin: 0 },
};

export default function MerchantDashboard() {
  return (
    <div style={s.page}>
      <Navbar role="MERCHANT" />
      <h2 style={s.h2}>🏪 Merchant Dashboard</h2>
      <div style={s.grid}>
        {STATS.map(stat => (
          <div key={stat.label} style={s.stat}>
            <div style={s.icon}>{stat.icon}</div>
            <div style={{ ...s.value, color: stat.color }}>{stat.value}</div>
            <div style={s.label}>{stat.label}</div>
          </div>
        ))}
      </div>
      <div style={{ ...s.grid, maxWidth: 700, paddingTop: 0 }}>
        <Link to="/merchant/products" style={s.navCard}><div style={{ fontSize: 30 }}>🛍️</div><div style={{ fontWeight: 600, marginTop: 8 }}>Manage Products</div></Link>
        <Link to="/merchant/orders" style={s.navCard}><div style={{ fontSize: 30 }}>📦</div><div style={{ fontWeight: 600, marginTop: 8 }}>View Orders</div></Link>
        <Link to="/merchant/profile" style={s.navCard}><div style={{ fontSize: 30 }}>🏪</div><div style={{ fontWeight: 600, marginTop: 8 }}>Shop Profile</div></Link>
      </div>
    </div>
  );
}
