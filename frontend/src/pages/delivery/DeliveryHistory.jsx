import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getDeliveryHistoryApi } from '../../api/orderApi';

export default function DeliveryHistory() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getDeliveryHistoryApi().then(r => setHistory(r.data || []))
      .catch(() => setError('Failed to load')).finally(() => setLoading(false));
  }, []);

  const totalEarnings = history.filter(o => o.status === 'DELIVERED').reduce((s, o) => s + (o.deliveryFee || 0), 0);

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <div style={styles.header}>
          <h2 style={styles.heading}>📋 Delivery History</h2>
          <div style={styles.earnings}>Total Earnings: <span style={{ color: '#e94560', fontWeight: 700 }}>₹{totalEarnings}</span></div>
        </div>
        <ErrorMsg msg={error} />
        {loading ? <Loader /> : history.length === 0 ? <p style={{ color: '#888' }}>No history yet.</p> : (
          <table style={styles.table}>
            <thead><tr style={styles.th}>{['Order ID','Shop','Customer','Amount','Fee','Date','Status'].map(h => <th key={h} style={styles.thCell}>{h}</th>)}</tr></thead>
            <tbody>
              {history.map(o => (
                <tr key={o.id} style={styles.tr}>
                  <td style={styles.td}>#{o.id}</td>
                  <td style={styles.td}>{o.shopName}</td>
                  <td style={styles.td}>{o.customerName}</td>
                  <td style={styles.td}>₹{o.totalAmount}</td>
                  <td style={{ ...styles.td, color: '#22c55e', fontWeight: 600 }}>₹{o.deliveryFee || 40}</td>
                  <td style={styles.td}>{new Date(o.createdAt).toLocaleDateString()}</td>
                  <td style={styles.td}><span style={{ ...styles.badge, background: o.status === 'DELIVERED' ? '#22c55e' : '#ef4444' }}>{o.status}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 },
  heading: { color: '#1a1a2e' },
  earnings: { background: '#fff', borderRadius: 8, padding: '8px 16px', boxShadow: '0 2px 6px rgba(0,0,0,0.07)' },
  table: { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 10, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  th: { background: '#1a1a2e', color: '#fff' },
  thCell: { padding: '10px 14px', textAlign: 'left', fontSize: 13 },
  tr: { borderBottom: '1px solid #f0f0f0' },
  td: { padding: '10px 14px', fontSize: 14 },
  badge: { borderRadius: 12, padding: '2px 10px', color: '#fff', fontSize: 12 },
};
