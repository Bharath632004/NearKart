import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import { getMerchantProductsApi } from '../../api/productApi';
import { getMerchantOrdersApi } from '../../api/orderApi';

export default function MerchantDashboard() {
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getMerchantProductsApi(), getMerchantOrdersApi()])
      .then(([p, o]) => { setProducts(p.data || []); setOrders(o.data || []); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const revenue = orders.filter(o => o.status === 'DELIVERED').reduce((s, o) => s + o.totalAmount, 0);
  const stats = [
    { label: 'Total Products', value: products.length, icon: '📦', link: '/merchant/products' },
    { label: 'Total Orders', value: orders.length, icon: '🧾', link: '/merchant/orders' },
    { label: 'Pending Orders', value: orders.filter(o => o.status === 'PENDING').length, icon: '⏳', link: '/merchant/orders' },
    { label: 'Revenue', value: `₹${revenue}`, icon: '💰', link: '/merchant/orders' },
  ];

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>🏪 Merchant Dashboard</h2>
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
            <h3 style={{ marginTop: 32 }}>Recent Orders</h3>
            {orders.length === 0 ? <p style={{ color: '#888' }}>No orders yet.</p> : (
              <table style={styles.table}>
                <thead><tr style={styles.th}>{['Order ID','Customer','Amount','Status','Date'].map(h => <th key={h} style={styles.thCell}>{h}</th>)}</tr></thead>
                <tbody>
                  {orders.slice(0, 5).map(o => (
                    <tr key={o.id} style={styles.tr}>
                      <td style={styles.td}>#{o.id}</td>
                      <td style={styles.td}>{o.customerName}</td>
                      <td style={styles.td}>₹{o.totalAmount}</td>
                      <td style={styles.td}><span style={{ ...styles.badge, background: sc(o.status) }}>{o.status}</span></td>
                      <td style={styles.td}>{new Date(o.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </>
        )}
      </div>
    </div>
  );
}

const sc = (s) => ({ PENDING:'#f59e0b', CONFIRMED:'#3b82f6', DELIVERED:'#22c55e', CANCELLED:'#ef4444' }[s] || '#888');
const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 24 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16 },
  card: { background: '#fff', borderRadius: 12, padding: 20, textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', textDecoration: 'none', display: 'block' },
  icon: { fontSize: 32, marginBottom: 8 },
  value: { fontSize: 26, fontWeight: 700, color: '#e94560' },
  label: { color: '#666', fontSize: 13 },
  table: { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 10, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  th: { background: '#1a1a2e', color: '#fff' },
  thCell: { padding: '10px 14px', textAlign: 'left', fontSize: 13 },
  tr: { borderBottom: '1px solid #f0f0f0' },
  td: { padding: '10px 14px', fontSize: 14 },
  badge: { borderRadius: 12, padding: '2px 10px', color: '#fff', fontSize: 12 },
};
