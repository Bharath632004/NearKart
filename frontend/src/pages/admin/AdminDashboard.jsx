import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';

const STATS = [
  { label: 'Total Users', value: 1245, icon: '👥', color: '#1a73e8' },
  { label: 'Active Merchants', value: 48, icon: '🏪', color: '#7b1fa2' },
  { label: 'Delivery Partners', value: 23, icon: '🚴', color: '#e65100' },
  { label: "Today's Orders", value: 312, icon: '📦', color: '#2e7d32' },
  { label: "Today's Revenue", value: '₹38,540', icon: '💰', color: '#00838f' },
  { label: 'Pending Issues', value: 7, icon: '⚠️', color: '#c62828' },
];

const NAV_CARDS = [
  { to: '/admin/users', icon: '👥', label: 'Manage Users' },
  { to: '/admin/merchants', icon: '🏪', label: 'Manage Merchants' },
  { to: '/admin/delivery', icon: '🚴', label: 'Delivery Partners' },
  { to: '/admin/reports', icon: '📊', label: 'Reports & Analytics' },
];

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(180px,1fr))', gap: 16, padding: 32, maxWidth: 1100, margin: '0 auto' },
  stat: { background: '#fff', borderRadius: 12, padding: 20, boxShadow: '0 2px 10px rgba(0,0,0,.07)', textAlign: 'center' },
  navCard: { background: '#fff', borderRadius: 12, padding: 22, boxShadow: '0 2px 10px rgba(0,0,0,.07)', textDecoration: 'none', color: '#222', textAlign: 'center' },
};

export default function AdminDashboard() {
  return (
    <div style={s.page}>
      <Navbar role="ADMIN" />
      <h2 style={{ padding: '24px 32px 0', margin: 0 }}>⚙️ Admin Dashboard</h2>
      <div style={s.grid}>
        {STATS.map(st => (
          <div key={st.label} style={s.stat}>
            <div style={{ fontSize: 34 }}>{st.icon}</div>
            <div style={{ fontSize: 24, fontWeight: 700, color: st.color, marginTop: 6 }}>{st.value}</div>
            <div style={{ color: '#666', fontSize: 12, marginTop: 4 }}>{st.label}</div>
          </div>
        ))}
      </div>
      <div style={{ ...s.grid, maxWidth: 900, paddingTop: 0 }}>
        {NAV_CARDS.map(c => (
          <Link key={c.to} to={c.to} style={s.navCard}>
            <div style={{ fontSize: 32 }}>{c.icon}</div>
            <div style={{ fontWeight: 600, marginTop: 10 }}>{c.label}</div>
          </Link>
        ))}
      </div>
    </div>
  );
}
