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
    { label: "Today's Orders", value: report.todayOrders || 0, icon: '📦', link: '/admin/reports' },
    { label: 'Revenue', value: `₹${report.totalRevenue || 0}`, icon: '💰', link: '/admin/reports' },
    { label: 'Pending Issues', value: report.pendingIssues || 0, icon: '⚠️', link: '/admin/reports' },
  ];

  const navCards = [
    { to: '/admin/users', icon: '👥', label: 'Manage Users' },
    { to: '/admin/merchants', icon: '🏪', label: 'Manage Merchants' },
    { to: '/admin/delivery', icon: '🚴', label: 'Delivery Partners' },
    { to: '/admin/reports', icon: '📊', label: 'Reports & Analytics' },
  ];

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>⚙️ Admin Dashboard</h2>
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
            <h3 style={{ marginTop: 32 }}>Quick Actions</h3>
            <div style={styles.navGrid}>
              {navCards.map(c => (
                <Link key={c.to} to={c.to} style={styles.navCard}>
                  <div style={styles.navIcon}>{c.icon}</div>
                  <div style={styles.navLabel}>{c.label}</div>
                </Link>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 24 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 16 },
  card: { background: '#fff', borderRadius: 12, padding: 20, textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', textDecoration: 'none', display: 'block' },
  icon: { fontSize: 32, marginBottom: 8 },
  value: { fontSize: 26, fontWeight: 700, color: '#e94560' },
  label: { color: '#666', fontSize: 13 },
  navGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16, marginTop: 12 },
  navCard: { background: '#fff', borderRadius: 12, padding: 22, textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', textDecoration: 'none', display: 'block' },
  navIcon: { fontSize: 32, marginBottom: 8 },
  navLabel: { fontWeight: 600, color: '#1a1a2e', fontSize: 14 },
};
