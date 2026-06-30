import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getCustomerOrdersApi } from '../../api/orderApi';

const statusColor = (s) => ({ PENDING:'#f59e0b', CONFIRMED:'#3b82f6', OUT_FOR_DELIVERY:'#8b5cf6', DELIVERED:'#22c55e', CANCELLED:'#ef4444' }[s] || '#888');

export default function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expanded, setExpanded] = useState(null);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    getCustomerOrdersApi().then(r => setOrders(r.data || []))
      .catch(() => setError('Failed to load orders'))
      .finally(() => setLoading(false));
  }, []);

  const statuses = ['ALL', 'PENDING', 'CONFIRMED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];
  const filtered = filter === 'ALL' ? orders : orders.filter(o => o.status === filter);

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>📦 My Orders</h2>
        <ErrorMsg msg={error} />
        <div style={styles.filters}>
          {statuses.map(s => (
            <button key={s} style={{ ...styles.filterBtn, background: filter === s ? '#e94560' : '#eee', color: filter === s ? '#fff' : '#333' }}
              onClick={() => setFilter(s)}>{s}</button>
          ))}
        </div>
        {loading ? <Loader /> : filtered.length === 0 ? <p style={{ color: '#888' }}>No orders found.</p> : (
          <div>
            {filtered.map(o => (
              <div key={o.id} style={styles.card}>
                <div style={styles.cardHead} onClick={() => setExpanded(expanded === o.id ? null : o.id)}>
                  <div>
                    <span style={styles.orderId}>Order #{o.id}</span>
                    <span style={styles.shop}>{o.shopName}</span>
                  </div>
                  <div style={styles.right}>
                    <span style={{ ...styles.badge, background: statusColor(o.status) }}>{o.status}</span>
                    <span style={styles.amount}>₹{o.totalAmount}</span>
                    <span style={styles.date}>{new Date(o.createdAt).toLocaleDateString()}</span>
                    <span>{expanded === o.id ? '▲' : '▼'}</span>
                  </div>
                </div>
                {expanded === o.id && o.items && (
                  <div style={styles.items}>
                    {o.items.map(i => (
                      <div key={i.id} style={styles.itemRow}>
                        <span>{i.productName}</span>
                        <span>x{i.quantity}</span>
                        <span>₹{i.price * i.quantity}</span>
                      </div>
                    ))}
                    <div style={{ marginTop: 8, fontSize: 13, color: '#888' }}>📍 {o.deliveryAddress}</div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '24px 32px', maxWidth: 900, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 16 },
  filters: { display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 20 },
  filterBtn: { padding: '6px 14px', border: 'none', borderRadius: 20, cursor: 'pointer', fontSize: 12 },
  card: { background: '#fff', borderRadius: 10, marginBottom: 12, boxShadow: '0 2px 6px rgba(0,0,0,0.07)' },
  cardHead: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 18px', cursor: 'pointer', flexWrap: 'wrap', gap: 8 },
  orderId: { fontWeight: 700, fontSize: 15, color: '#1a1a2e', marginRight: 10 },
  shop: { color: '#666', fontSize: 14 },
  right: { display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' },
  badge: { borderRadius: 12, padding: '2px 10px', color: '#fff', fontSize: 11 },
  amount: { fontWeight: 700, color: '#e94560' },
  date: { color: '#888', fontSize: 12 },
  items: { borderTop: '1px solid #f0f0f0', padding: '12px 18px' },
  itemRow: { display: 'flex', justifyContent: 'space-between', fontSize: 14, padding: '4px 0', color: '#444' },
};
