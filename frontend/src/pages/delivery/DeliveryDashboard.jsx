import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';

const STATS = [
  { label: "Today's Deliveries", value: 8, icon: '🚴', color: '#1a73e8' },
  { label: 'Completed', value: 5, icon: '✅', color: '#2e7d32' },
  { label: 'Pending', value: 3, icon: '⏳', color: '#e65100' },
  { label: "Today's Earnings", value: '₹480', icon: '💰', color: '#7b1fa2' },
];

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(200px,1fr))', gap: 20, padding: 32, maxWidth: 1000, margin: '0 auto' },
  stat: { background: '#fff', borderRadius: 12, padding: 24, boxShadow: '0 2px 10px rgba(0,0,0,.07)', textAlign: 'center' },
  navCard: { background: '#fff', borderRadius: 12, padding: 20, boxShadow: '0 2px 10px rgba(0,0,0,.07)', textDecoration: 'none', color: '#222', textAlign: 'center' },
};

export default function DeliveryDashboard() {
  return (
    <div style={s.page}>
      <Navbar role="DELIVERY" />
      <h2 style={{ padding: '24px 32px 0', margin: 0 }}>🚴 Delivery Dashboard</h2>
      <div style={s.grid}>
        {STATS.map(st => (
          <div key={st.label} style={s.stat}>
            <div style={{ fontSize: 36 }}>{st.icon}</div>
            <div style={{ fontSize: 26, fontWeight: 700, color: st.color, marginTop: 8 }}>{st.value}</div>
            <div style={{ color: '#666', fontSize: 13 }}>{st.label}</div>
          </div>
        ))}
      </div>
      <div style={{ ...s.grid, maxWidth: 700, paddingTop: 0 }}>
        <Link to="/delivery/active" style={s.navCard}><div style={{ fontSize: 30 }}>📍</div><div style={{ fontWeight: 600, marginTop: 8 }}>Active Deliveries</div></Link>
        <Link to="/delivery/history" style={s.navCard}><div style={{ fontSize: 30 }}>📋</div><div style={{ fontWeight: 600, marginTop: 8 }}>Delivery History</div></Link>
        <Link to="/delivery/profile" style={s.navCard}><div style={{ fontSize: 30 }}>👤</div><div style={{ fontWeight: 600, marginTop: 8 }}>My Profile</div></Link>
      </div>
    </div>
  );
}
