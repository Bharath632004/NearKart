import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getDeliveryOrdersApi, acceptDeliveryApi, completeDeliveryApi } from '../../api/orderApi';

export default function ActiveDeliveries() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = () => getDeliveryOrdersApi().then(r => setOrders(r.data || []))
    .catch(() => setError('Failed to load')).finally(() => setLoading(false));

  useEffect(() => { load(); }, []);

  const handleAccept = async (id) => {
    try { await acceptDeliveryApi(id); load(); } catch { setError('Failed to accept'); }
  };
  const handleComplete = async (id) => {
    try { await completeDeliveryApi(id); load(); } catch { setError('Failed to complete'); }
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>🚴 Active Deliveries</h2>
        <ErrorMsg msg={error} />
        {loading ? <Loader /> : orders.length === 0 ? <p style={{ color: '#888' }}>No deliveries available.</p> : (
          orders.map(o => (
            <div key={o.id} style={styles.card}>
              <div style={styles.row}>
                <div>
                  <div style={styles.orderId}>Order #{o.id}</div>
                  <div style={styles.meta}>🏪 {o.shopName} → 📍 {o.deliveryAddress}</div>
                  <div style={styles.meta}>Items: {o.items?.length || 0} | Amount: ₹{o.totalAmount} | Fee: ₹{o.deliveryFee || 40}</div>
                </div>
                <div style={{ display: 'flex', gap: 8, flexDirection: 'column' }}>
                  <span style={{ ...styles.badge, background: sc(o.status) }}>{o.status}</span>
                  {o.status === 'READY_FOR_PICKUP' && (
                    <button style={styles.acceptBtn} onClick={() => handleAccept(o.id)}>Accept</button>
                  )}
                  {o.status === 'OUT_FOR_DELIVERY' && (
                    <button style={styles.doneBtn} onClick={() => handleComplete(o.id)}>Mark Delivered</button>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

const sc = (s) => ({ READY_FOR_PICKUP:'#8b5cf6', OUT_FOR_DELIVERY:'#3b82f6', DELIVERED:'#22c55e' }[s] || '#888');
const styles = {
  page: { padding: '24px 32px', maxWidth: 900, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 20 },
  card: { background: '#fff', borderRadius: 10, padding: '16px 20px', marginBottom: 12, boxShadow: '0 2px 6px rgba(0,0,0,0.07)' },
  row: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 },
  orderId: { fontWeight: 700, fontSize: 15, color: '#1a1a2e' },
  meta: { color: '#666', fontSize: 13, marginTop: 4 },
  badge: { borderRadius: 12, padding: '3px 12px', color: '#fff', fontSize: 12, textAlign: 'center' },
  acceptBtn: { background: '#3b82f6', color: '#fff', border: 'none', borderRadius: 8, padding: '7px 14px', cursor: 'pointer', fontWeight: 600 },
  doneBtn: { background: '#22c55e', color: '#fff', border: 'none', borderRadius: 8, padding: '7px 14px', cursor: 'pointer', fontWeight: 600 },
};
