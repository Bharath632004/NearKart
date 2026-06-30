import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import { getShopsApi } from '../../api/shopApi';
import { getCustomerOrdersApi } from '../../api/orderApi';
import { useSelector } from 'react-redux';

export default function CustomerDashboard() {
  const cartItems = useSelector((s) => s.cart.items);
  const [shops, setShops] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getShopsApi(), getCustomerOrdersApi()])
      .then(([s, o]) => { setShops(s.data || []); setOrders(o.data || []); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const stats = [
    { label: 'Nearby Shops', value: shops.length, icon: '🏪', link: '/customer/shops' },
    { label: 'Cart Items', value: cartItems.length, icon: '🛒', link: '/customer/cart' },
    { label: 'Total Orders', value: orders.length, icon: '📦', link: '/customer/orders' },
    { label: 'Active Orders', value: orders.filter(o => !['DELIVERED','CANCELLED'].includes(o.status)).length, icon: '🚚', link: '/customer/orders' },
  ];

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>Welcome Back! 👋</h2>
        {loading ? <Loader /> : (
          <>
            <div style={styles.grid}>
              {stats.map((s) => (
                <Link to={s.link} key={s.label} style={styles.card}>
                  <div style={styles.icon}>{s.icon}</div>
                  <div style={styles.value}>{s.value}</div>
                  <div style={styles.label}>{s.label}</div>
                </Link>
              ))}
            </div>
            <h3 style={{ marginTop: 32 }}>Recent Orders</h3>
            {orders.length === 0 ? <p style={{ color: '#888' }}>No orders yet. <Link to="/customer/shops" style={{ color: '#e94560' }}>Start shopping!</Link></p> : (
              <table style={styles.table}>
                <thead><tr style={styles.th}>{['Order ID','Shop','Amount','Status','Date'].map(h => <th key={h} style={styles.thCell}>{h}</th>)}</tr></thead>
                <tbody>
                  {orders.slice(0, 5).map(o => (
                    <tr key={o.id} style={styles.tr}>
                      <td style={styles.td}>#{o.id}</td>
                      <td style={styles.td}>{o.shopName}</td>
                      <td style={styles.td}>₹{o.totalAmount}</td>
                      <td style={styles.td}><span style={{ ...styles.badge, background: statusColor(o.status) }}>{o.status}</span></td>
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

const statusColor = (s) => ({ PENDING:'#f59e0b', CONFIRMED:'#3b82f6', DELIVERED:'#22c55e', CANCELLED:'#ef4444' }[s] || '#888');

const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 24 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16 },
  card: { background: '#fff', borderRadius: 12, padding: 20, textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', textDecoration: 'none', display: 'block' },
  icon: { fontSize: 32, marginBottom: 8 },
  value: { fontSize: 28, fontWeight: 700, color: '#e94560' },
  label: { color: '#666', fontSize: 13 },
  table: { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 10, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  th: { background: '#1a1a2e', color: '#fff' },
  thCell: { padding: '10px 14px', textAlign: 'left', fontSize: 13 },
  tr: { borderBottom: '1px solid #f0f0f0' },
  td: { padding: '10px 14px', fontSize: 14 },
  badge: { borderRadius: 12, padding: '2px 10px', color: '#fff', fontSize: 12 },
};
