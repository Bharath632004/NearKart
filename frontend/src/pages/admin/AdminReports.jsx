import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const WEEKLY = [
  { day: 'Mon', orders: 280, revenue: 34200 },
  { day: 'Tue', orders: 312, revenue: 38540 },
  { day: 'Wed', orders: 295, revenue: 36100 },
  { day: 'Thu', orders: 340, revenue: 42000 },
  { day: 'Fri', orders: 390, revenue: 48500 },
  { day: 'Sat', orders: 450, revenue: 56000 },
  { day: 'Sun', orders: 320, revenue: 39200 },
];

const TOP_SHOPS = [
  { name: 'Sri Lakshmi Grocery', orders: 148, revenue: 45200 },
  { name: 'Fresh Vegetables', orders: 125, revenue: 32100 },
  { name: 'Raju Medical Store', orders: 98, revenue: 29800 },
  { name: 'Annapurna Bakery', orders: 87, revenue: 21700 },
];

const maxRev = Math.max(...WEEKLY.map(d => d.revenue));
// fix: compute maxOrders dynamically instead of hardcoded 450
const maxOrders = Math.max(...WEEKLY.map(d => d.orders));

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 1000, margin: '0 auto', padding: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 24, boxShadow: '0 2px 10px rgba(0,0,0,.07)', marginBottom: 24 },
  th: { background: '#1a73e8', color: '#fff', padding: '10px 14px', textAlign: 'left' },
  td: { padding: '10px 14px', borderBottom: '1px solid #f0f0f0' },
  bar: { height: 28, borderRadius: 6, background: '#1a73e8', marginBottom: 4, display: 'flex', alignItems: 'center', paddingLeft: 8, color: '#fff', fontSize: 12, fontWeight: 600 },
};

export default function AdminReports() {
  const [tab, setTab] = useState('revenue');
  const totalRevenue = WEEKLY.reduce((a, b) => a + b.revenue, 0);
  const totalOrders = WEEKLY.reduce((a, b) => a + b.orders, 0);
  return (
    <div style={s.page}>
      <Navbar role="ADMIN" />
      <div style={s.container}>
        <h2>📊 Reports & Analytics</h2>
        <div style={{ display: 'flex', gap: 16, marginBottom: 24 }}>
          {[['revenue', 'Weekly Revenue'], ['orders', 'Weekly Orders'], ['shops', 'Top Shops']].map(([key, label]) => (
            <button key={key} onClick={() => setTab(key)} style={{ padding: '8px 20px', borderRadius: 8, border: 'none', cursor: 'pointer', fontWeight: 600, background: tab === key ? '#1a73e8' : '#e8eaf6', color: tab === key ? '#fff' : '#333' }}>{label}</button>
          ))}
        </div>

        {tab === 'revenue' && (
          <div style={s.card}>
            <h3 style={{ marginTop: 0 }}>Weekly Revenue — Total: ₹{totalRevenue.toLocaleString()}</h3>
            {WEEKLY.map(d => (
              <div key={d.day} style={{ marginBottom: 8 }}>
                <div style={{ fontSize: 12, color: '#666', marginBottom: 2 }}>{d.day}</div>
                <div style={{ ...s.bar, width: `${(d.revenue / maxRev) * 100}%` }}>₹{d.revenue.toLocaleString()}</div>
              </div>
            ))}
          </div>
        )}

        {tab === 'orders' && (
          <div style={s.card}>
            <h3 style={{ marginTop: 0 }}>Weekly Orders — Total: {totalOrders}</h3>
            {WEEKLY.map(d => (
              <div key={d.day} style={{ marginBottom: 8 }}>
                <div style={{ fontSize: 12, color: '#666', marginBottom: 2 }}>{d.day}</div>
                {/* fix: use dynamic maxOrders instead of hardcoded 450 */}
                <div style={{ ...s.bar, width: `${(d.orders / maxOrders) * 100}%`, background: '#7b1fa2' }}>{d.orders} orders</div>
              </div>
            ))}
          </div>
        )}

        {tab === 'shops' && (
          <div style={s.card}>
            <h3 style={{ marginTop: 0 }}>Top Performing Shops</h3>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead><tr>{['Shop', 'Orders', 'Revenue'].map(h => <th key={h} style={s.th}>{h}</th>)}</tr></thead>
              <tbody>{TOP_SHOPS.map(sh => (<tr key={sh.name}><td style={s.td}>{sh.name}</td><td style={s.td}>{sh.orders}</td><td style={s.td}>₹{sh.revenue.toLocaleString()}</td></tr>))}</tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
