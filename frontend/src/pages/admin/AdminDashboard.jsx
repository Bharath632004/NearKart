import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import { getAdminReportsApi } from '../../api/adminApi';

export default function AdminDashboard() {
  const [report, setReport] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAdminReportsApi().then(r => setReport(r.data || {}))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const stats = [
    { label: 'Total Users', value: report.totalUsers || 0, icon: '👥', link: '/admin/users' },
    { label: 'Merchants', value: report.totalMerchants || 0, icon: '🏪', link: '/admin/merchants' },
    { label: 'Delivery Partners', value: report.totalDelivery || 0, icon: '🚴', link: '/admin/delivery' },
    { label: 'Total Orders', value: report.totalOrders || 0, icon: '📦', link: '/admin/reports' },
    { label: 'Revenue', value: `₹${report.totalRevenue || 0}`, icon: '💰', link: '/admin/reports' },
    { label: 'Pending Approvals', value: report.pendingApprovals || 0, icon: '⏳', link: '/admin/merchants' },
  ];

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>🔧 Admin Dashboard</h2>
        {loading ? <Loader /> : (
          <div style={styles.grid}>
            {stats.map(s => (
              <Link to={s.link} key={s.label} style={styles.card}>
                <div style={styles.icon}>{s.icon}</div>
                <div style={styles.value}>{s.value}</div>
                <div style={styles.label}>{s.label}</div>
              </Link>
            ))}
          </div>
        )}
        <div style={styles.quickLinks}>
          <h3>Quick Actions</h3>
          <div style={styles.linkGrid}>
            {[{to:'/admin/users',label:'Manage Users',icon:'👥'},{to:'/admin/merchants',label:'Approve Merchants',icon:'🏪'},
              {to:'/admin/delivery',label:'Delivery Partners',icon:'🚴'},{to:'/admin/reports',label:'View Reports',icon:'📊'}].map(l => (
              <Link to={l.to} key={l.to} style={styles.ql}>
                <span style={{ fontSize: 28 }}>{l.icon}</span>
                <span style={{ marginTop: 6, fontSize: 13 }}>{l.label}</span>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 24 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 16, marginBottom: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 20, textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', textDecoration: 'none', display: 'block' },
  icon: { fontSize: 32, marginBottom: 8 },
  value: { fontSize: 26, fontWeight: 700, color: '#e94560' },
  label: { color: '#666', fontSize: 13 },
  quickLinks: { background: '#fff', borderRadius: 12, padding: 24, boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  linkGrid: { display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, marginTop: 12 },
  ql: { display: 'flex', flexDirection: 'column', alignItems: 'center', padding: 16, borderRadius: 10, border: '1px solid #eee', textDecoration: 'none', color: '#333', fontSize: 14, transition: 'all 0.2s' },
};
