import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getMerchantOrdersApi, updateOrderStatusApi } from '../../api/orderApi';

const NEXT = { PENDING: 'CONFIRMED', CONFIRMED: 'READY_FOR_PICKUP' };
const sc = (s) => ({ PENDING:'#f59e0b', CONFIRMED:'#3b82f6', READY_FOR_PICKUP:'#8b5cf6', OUT_FOR_DELIVERY:'#06b6d4', DELIVERED:'#22c55e', CANCELLED:'#ef4444' }[s] || '#888');

export default function MerchantOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');

  const load = () => getMerchantOrdersApi().then(r => setOrders(r.data || []))
    .catch(() => setError('Failed to load')).finally(() => setLoading(false));

  useEffect(() => { load(); }, []);

  const handleStatus = async (id, status) => {
    try { await updateOrderStatusApi(id, status); load(); }
    catch { setError('Status update failed'); }
  };

  const statuses = ['ALL', 'PENDING', 'CONFIRMED', 'READY_FOR_PICKUP', 'DELIVERED', 'CANCELLED'];
  const filtered = filter === 'ALL' ? orders : orders.filter(o => o.status === filter);

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>🧾 Merchant Orders</h2>
        <ErrorMsg msg={error} />
        <div style={styles.filters}>
          {statuses.map(s => (
            <button key={s} style={{ ...styles.fBtn, background: filter === s ? '#e94560' : '#eee', color: filter === s ? '#fff' : '#333' }}
              onClick={() => setFilter(s)}>{s}</button>
          ))}
        </div>
        {loading ? <Loader /> : filtered.length === 0 ? <p style={{ color: '#888' }}>No orders.</p> : (
          <table style={styles.table}>
            <thead><tr style={styles.th}>{['Order ID','Customer','Items','Amount','Status','Action'].map(h => <th key={h} style={styles.thCell}>{h}</th>)}</tr></thead>
            <tbody>
              {filtered.map(o => (
                <tr key={o.id} style={styles.tr}>
                  <td style={styles.td}>#{o.id}</td>
                  <td style={styles.td}>{o.customerName}</td>
                  <td style={styles.td}>{o.items?.length || 0} items</td>
                  <td style={styles.td}>₹{o.totalAmount}</td>
                  <td style={styles.td}><span style={{ ...styles.badge, background: sc(o.status) }}>{o.status}</span></td>
                  <td style={styles.td}>
                    {NEXT[o.status] && (
                      <button style={styles.actBtn} onClick={() => handleStatus(o.id, NEXT[o.status])}>
                        Mark {NEXT[o.status].replace('_',' ')}
                      </button>
                    )}
                  </td>
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
  heading: { color: '#1a1a2e', marginBottom: 16 },
  filters: { display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 20 },
  fBtn: { padding: '6px 14px', border: 'none', borderRadius: 20, cursor: 'pointer', fontSize: 12 },
  table: { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 10, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  th: { background: '#1a1a2e', color: '#fff' },
  thCell: { padding: '10px 14px', textAlign: 'left', fontSize: 13 },
  tr: { borderBottom: '1px solid #f0f0f0' },
  td: { padding: '10px 14px', fontSize: 14 },
  badge: { borderRadius: 12, padding: '2px 10px', color: '#fff', fontSize: 12 },
  actBtn: { background: '#3b82f6', color: '#fff', border: 'none', borderRadius: 6, padding: '5px 12px', cursor: 'pointer', fontSize: 12 },
};
