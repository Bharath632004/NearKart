import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getAllDeliveryPartnersApi, approveDeliveryPartnerApi } from '../../api/adminApi';

export default function ManageDelivery() {
  const [partners, setPartners] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');

  const load = () => getAllDeliveryPartnersApi().then(r => setPartners(r.data || []))
    .catch(() => setError('Failed to load')).finally(() => setLoading(false));

  useEffect(() => { load(); }, []);

  const handleApprove = async (id) => {
    try { await approveDeliveryPartnerApi(id); load(); } catch { setError('Approve failed'); }
  };

  const statuses = ['ALL', 'PENDING', 'APPROVED'];
  const filtered = filter === 'ALL' ? partners : partners.filter(p => p.status === filter);
  const sc = (s) => ({ PENDING:'#f59e0b', APPROVED:'#22c55e' }[s] || '#888');

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>🚴 Manage Delivery Partners</h2>
        <div style={styles.filters}>
          {statuses.map(s => (
            <button key={s} style={{ ...styles.fBtn, background: filter === s ? '#e94560' : '#eee', color: filter === s ? '#fff' : '#333' }}
              onClick={() => setFilter(s)}>{s}</button>
          ))}
        </div>
        <ErrorMsg msg={error} />
        {loading ? <Loader /> : filtered.length === 0 ? <p style={{ color: '#888' }}>No delivery partners.</p> : (
          <table style={styles.table}>
            <thead><tr style={styles.th}>{['Name','Email','Phone','Vehicle','License','Status','Action'].map(h => <th key={h} style={styles.thCell}>{h}</th>)}</tr></thead>
            <tbody>
              {filtered.map(p => (
                <tr key={p.id} style={styles.tr}>
                  <td style={styles.td}>{p.name}</td>
                  <td style={styles.td}>{p.email}</td>
                  <td style={styles.td}>{p.phone || '-'}</td>
                  <td style={styles.td}>{p.vehicleType || '-'}</td>
                  <td style={styles.td}>{p.licenseNo || '-'}</td>
                  <td style={styles.td}><span style={{ ...styles.badge, background: sc(p.status) }}>{p.status}</span></td>
                  <td style={styles.td}>
                    {p.status === 'PENDING' && (
                      <button style={styles.approveBtn} onClick={() => handleApprove(p.id)}>✅ Approve</button>
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
  filters: { display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 16 },
  fBtn: { padding: '6px 14px', border: 'none', borderRadius: 20, cursor: 'pointer', fontSize: 12 },
  table: { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 10, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  th: { background: '#1a1a2e', color: '#fff' },
  thCell: { padding: '10px 14px', textAlign: 'left', fontSize: 13 },
  tr: { borderBottom: '1px solid #f0f0f0' },
  td: { padding: '10px 14px', fontSize: 14 },
  badge: { borderRadius: 12, padding: '2px 10px', color: '#fff', fontSize: 12 },
  approveBtn: { background: '#22c55e', color: '#fff', border: 'none', borderRadius: 6, padding: '4px 12px', cursor: 'pointer', fontSize: 12 },
};
