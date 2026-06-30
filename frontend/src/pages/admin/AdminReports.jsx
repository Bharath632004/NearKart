import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getAdminReportsApi } from '../../api/adminApi';

export default function AdminReports() {
  const [report, setReport] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getAdminReportsApi().then(r => setReport(r.data || {}))
      .catch(() => setError('Failed to load reports'))
      .finally(() => setLoading(false));
  }, []);

  const metrics = [
    { label: 'Total Revenue', value: `₹${report.totalRevenue || 0}`, icon: '💰', color: '#22c55e' },
    { label: 'Total Orders', value: report.totalOrders || 0, icon: '📦', color: '#3b82f6' },
    { label: 'Delivered Orders', value: report.deliveredOrders || 0, icon: '✅', color: '#22c55e' },
    { label: 'Cancelled Orders', value: report.cancelledOrders || 0, icon: '❌', color: '#ef4444' },
    { label: 'Active Users', value: report.activeUsers || 0, icon: '👥', color: '#8b5cf6' },
    { label: 'Active Merchants', value: report.activeMerchants || 0, icon: '🏪', color: '#f59e0b' },
  ];

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>📊 Admin Reports</h2>
        <ErrorMsg msg={error} />
        {loading ? <Loader /> : (
          <>
            <div style={styles.grid}>
              {metrics.map(m => (
                <div key={m.label} style={styles.card}>
                  <div style={styles.icon}>{m.icon}</div>
                  <div style={{ ...styles.value, color: m.color }}>{m.value}</div>
                  <div style={styles.label}>{m.label}</div>
                </div>
              ))}
            </div>
            {report.topMerchants?.length > 0 && (
              <div style={styles.section}>
                <h3 style={{ marginBottom: 16 }}>Top Merchants by Revenue</h3>
                <table style={styles.table}>
                  <thead><tr style={styles.th}>{['Merchant','Shop','Orders','Revenue'].map(h => <th key={h} style={styles.thCell}>{h}</th>)}</tr></thead>
                  <tbody>
                    {report.topMerchants.map((m, i) => (
                      <tr key={i} style={styles.tr}>
                        <td style={styles.td}>{m.name}</td>
                        <td style={styles.td}>{m.shopName}</td>
                        <td style={styles.td}>{m.orders}</td>
                        <td style={{ ...styles.td, color: '#22c55e', fontWeight: 700 }}>₹{m.revenue}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            {report.recentOrders?.length > 0 && (
              <div style={styles.section}>
                <h3 style={{ marginBottom: 16 }}>Recent Orders</h3>
                <table style={styles.table}>
                  <thead><tr style={styles.th}>{['Order ID','Customer','Merchant','Amount','Status','Date'].map(h => <th key={h} style={styles.thCell}>{h}</th>)}</tr></thead>
                  <tbody>
                    {report.recentOrders.map(o => (
                      <tr key={o.id} style={styles.tr}>
                        <td style={styles.td}>#{o.id}</td>
                        <td style={styles.td}>{o.customerName}</td>
                        <td style={styles.td}>{o.merchantName}</td>
                        <td style={styles.td}>₹{o.totalAmount}</td>
                        <td style={styles.td}><span style={{ ...styles.badge, background: sc(o.status) }}>{o.status}</span></td>
                        <td style={styles.td}>{new Date(o.createdAt).toLocaleDateString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
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
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 16, marginBottom: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 20, textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.08)' },
  icon: { fontSize: 32, marginBottom: 8 },
  value: { fontSize: 26, fontWeight: 700 },
  label: { color: '#666', fontSize: 13 },
  section: { background: '#fff', borderRadius: 12, padding: 20, marginBottom: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  table: { width: '100%', borderCollapse: 'collapse' },
  th: { background: '#f8f9fa' },
  thCell: { padding: '10px 14px', textAlign: 'left', fontSize: 13, color: '#444', borderBottom: '2px solid #eee' },
  tr: { borderBottom: '1px solid #f0f0f0' },
  td: { padding: '10px 14px', fontSize: 14 },
  badge: { borderRadius: 12, padding: '2px 10px', color: '#fff', fontSize: 12 },
};
