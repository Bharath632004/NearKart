import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import { getDeliveryOrdersApi, getDeliveryHistoryApi } from '../../api/orderApi';

export default function DeliveryDashboard() {
  const [active, setActive] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getDeliveryOrdersApi(), getDeliveryHistoryApi()])
      .then(([a, h]) => { setActive(a.data || []); setHistory(h.data || []); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const earnings = history.filter(o => o.status === 'DELIVERED').reduce((s, o) => s + (o.deliveryFee || 0), 0);
  const stats = [
    { label: 'Active Deliveries', value: active.length, icon: '🚴', link: '/delivery/active' },
    { label: 'Completed', value: history.filter(o => o.status === 'DELIVERED').length, icon: '✅', link: '/delivery/history' },
    { label: 'Total Earnings', value: `₹${earnings}`, icon: '💵', link: '/delivery/history' },
    { label: 'Total Trips', value: history.length, icon: '🗺️', link: '/delivery/history' },
  ];

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>🚴 Delivery Dashboard</h2>
        {loading ? <Loader /> : (
          <>
            <div style={styles.grid}>
              {stats.map(s => (
                <Link to={s.link} key={s.label} style={styles.card}>
                  <div style={styles.icon}>{s.icon}</div>
                  <div style={styles.value}>{s.value}</div>
                  <div style={styles.label}>{s.label}</div>
                </Link>
              ))}
            </div>
            <h3 style={{ marginTop: 32 }}>Active Deliveries</h3>
            {active.length === 0 ? <p style={{ color: '#888' }}>No active deliveries. <Link to="/delivery/active" style={{ color: '#e94560' }}>Check available orders.</Link></p> : (
              active.slice(0, 3).map(o => (
                <div key={o.id} style={styles.row}>
                  <span>Order #{o.id} — {o.shopName}</span>
                  <span style={{ color: '#e94560', fontWeight: 600 }}>₹{o.deliveryFee || 40}</span>
                  <span style={styles.badge}>{o.status}</span>
                </div>
              ))
            )}
          </>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 24 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16 },
  card: { background: '#fff', borderRadius: 12, padding: 20, textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', textDecoration: 'none', display: 'block' },
  icon: { fontSize: 32, marginBottom: 8 },
  value: { fontSize: 26, fontWeight: 700, color: '#e94560' },
  label: { color: '#666', fontSize: 13 },
  row: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#fff', borderRadius: 8, padding: '12px 16px', marginBottom: 8, boxShadow: '0 1px 4px rgba(0,0,0,0.05)' },
  badge: { background: '#3b82f6', color: '#fff', borderRadius: 12, padding: '2px 10px', fontSize: 12 },
};
