import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  hero: { background: 'linear-gradient(135deg,#1a73e8,#0d47a1)', color: '#fff', padding: '48px 32px', textAlign: 'center' },
  title: { fontSize: 32, fontWeight: 700, margin: 0 },
  subtitle: { fontSize: 16, marginTop: 10, opacity: 0.9 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(200px,1fr))', gap: 20, padding: 32, maxWidth: 900, margin: '0 auto' },
  card: { background: '#fff', borderRadius: 12, padding: 24, textAlign: 'center', boxShadow: '0 2px 12px rgba(0,0,0,.08)', textDecoration: 'none', color: '#222', transition: 'transform .2s' },
  icon: { fontSize: 36 },
  label: { marginTop: 12, fontWeight: 600, fontSize: 16 },
};

export default function CustomerDashboard() {
  const cards = [
    { icon: '🏪', label: 'Browse Shops', to: '/customer/shops' },
    { icon: '🛒', label: 'My Cart', to: '/customer/cart' },
    { icon: '📦', label: 'My Orders', to: '/customer/orders' },
    { icon: '👤', label: 'Profile', to: '/customer/profile' },
  ];
  return (
    <div style={s.page}>
      <Navbar role="CUSTOMER" />
      <div style={s.hero}>
        <p style={s.title}>Welcome back! 👋</p>
        <p style={s.subtitle}>Discover local shops near you</p>
      </div>
      <div style={s.grid}>
        {cards.map(c => (
          <Link key={c.to} to={c.to} style={s.card}>
            <div style={s.icon}>{c.icon}</div>
            <div style={s.label}>{c.label}</div>
          </Link>
        ))}
      </div>
    </div>
  );
}
