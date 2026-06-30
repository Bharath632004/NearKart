import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const MERCHANTS = [
  { id: 1, shop: 'Sri Lakshmi Grocery', owner: 'Venkat Rao', category: 'Grocery', status: 'Approved', revenue: 45200 },
  { id: 2, shop: 'Raju Medical Store', owner: 'Raju Kumar', category: 'Pharmacy', status: 'Approved', revenue: 32100 },
  { id: 3, shop: 'Mobile World', owner: 'Hari Prasad', category: 'Electronics', status: 'Pending', revenue: 0 },
  { id: 4, shop: 'Karthik Stationery', owner: 'Karthik', category: 'Stationery', status: 'Approved', revenue: 12800 },
  { id: 5, shop: 'New Cafe Corner', owner: 'Sneha Devi', category: 'Food', status: 'Pending', revenue: 0 },
];

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 1000, margin: '0 auto', padding: 32 },
  table: { width: '100%', background: '#fff', borderRadius: 12, overflow: 'hidden', boxShadow: '0 2px 10px rgba(0,0,0,.07)', borderCollapse: 'collapse' },
  th: { background: '#7b1fa2', color: '#fff', padding: '12px 16px', textAlign: 'left', fontWeight: 600 },
  td: { padding: '12px 16px', borderBottom: '1px solid #f0f0f0' },
  badge: { borderRadius: 12, padding: '2px 10px', fontSize: 12, fontWeight: 600 },
  btn: { border: 'none', borderRadius: 6, padding: '4px 12px', cursor: 'pointer', fontWeight: 600, marginRight: 6 },
};

export default function ManageMerchants() {
  const [merchants, setMerchants] = useState(MERCHANTS);
  const approve = (id) => setMerchants(m => m.map(x => x.id===id ? {...x, status:'Approved'} : x));
  const reject = (id) => setMerchants(m => m.filter(x => x.id!==id));
  return (
    <div style={s.page}>
      <Navbar role="ADMIN" />
      <div style={s.container}>
        <h2>🏪 Manage Merchants</h2>
        <table style={s.table}>
          <thead><tr>{['Shop','Owner','Category','Revenue','Status','Actions'].map(h => <th key={h} style={s.th}>{h}</th>)}</tr></thead>
          <tbody>{merchants.map(m => (
            <tr key={m.id}>
              <td style={s.td}>{m.shop}</td>
              <td style={s.td}>{m.owner}</td>
              <td style={s.td}>{m.category}</td>
              <td style={s.td}>₹{m.revenue.toLocaleString()}</td>
              <td style={s.td}><span style={{ ...s.badge, background: m.status==='Approved' ? '#e8f5e9' : '#fff3e0', color: m.status==='Approved' ? '#2e7d32' : '#e65100' }}>{m.status}</span></td>
              <td style={s.td}>
                {m.status==='Pending' && <button style={{ ...s.btn, background: '#e8f5e9', color: '#2e7d32' }} onClick={() => approve(m.id)}>Approve</button>}
                <button style={{ ...s.btn, background: '#ffebee', color: '#c62828' }} onClick={() => reject(m.id)}>Remove</button>
              </td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </div>
  );
}
