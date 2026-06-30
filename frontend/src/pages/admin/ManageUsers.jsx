import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const USERS = [
  { id: 1, name: 'C Bharath', email: 'bharath@example.com', role: 'CUSTOMER', status: 'Active', joined: '2026-01-10' },
  { id: 2, name: 'Priya Sharma', email: 'priya@example.com', role: 'CUSTOMER', status: 'Active', joined: '2026-02-14' },
  { id: 3, name: 'Venkat Rao', email: 'venkat@example.com', role: 'MERCHANT', status: 'Active', joined: '2026-01-05' },
  { id: 4, name: 'Ramesh Delivery', email: 'ramesh@example.com', role: 'DELIVERY', status: 'Active', joined: '2026-03-20' },
  { id: 5, name: 'Suresh Babu', email: 'suresh@example.com', role: 'CUSTOMER', status: 'Blocked', joined: '2026-04-01' },
];

const ROLE_COLORS = { CUSTOMER: '#1565c0', MERCHANT: '#7b1fa2', DELIVERY: '#e65100', ADMIN: '#2e7d32' };

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 1000, margin: '0 auto', padding: 32 },
  table: { width: '100%', background: '#fff', borderRadius: 12, overflow: 'hidden', boxShadow: '0 2px 10px rgba(0,0,0,.07)', borderCollapse: 'collapse' },
  th: { background: '#1a73e8', color: '#fff', padding: '12px 16px', textAlign: 'left', fontWeight: 600 },
  td: { padding: '12px 16px', borderBottom: '1px solid #f0f0f0' },
  badge: { borderRadius: 12, padding: '2px 10px', fontSize: 12, fontWeight: 600 },
  search: { padding: '10px 16px', border: '1px solid #ddd', borderRadius: 8, width: '100%', maxWidth: 350, marginBottom: 20, fontSize: 14 },
};

export default function ManageUsers() {
  const [users, setUsers] = useState(USERS);
  const [search, setSearch] = useState('');
  const toggle = (id) => setUsers(u => u.map(x => x.id === id ? { ...x, status: x.status === 'Active' ? 'Blocked' : 'Active' } : x));
  const filtered = users.filter(u => u.name.toLowerCase().includes(search.toLowerCase()) || u.email.toLowerCase().includes(search.toLowerCase()));
  return (
    <div style={s.page}>
      <Navbar role="ADMIN" />
      <div style={s.container}>
        <h2>👥 Manage Users</h2>
        <input style={s.search} placeholder="Search by name or email..." value={search} onChange={e => setSearch(e.target.value)} />
        <table style={s.table}>
          <thead><tr>{['Name','Email','Role','Joined','Status','Action'].map(h => <th key={h} style={s.th}>{h}</th>)}</tr></thead>
          <tbody>{filtered.map(u => (
            <tr key={u.id}>
              <td style={s.td}>{u.name}</td>
              <td style={s.td}>{u.email}</td>
              <td style={s.td}><span style={{ ...s.badge, background: (ROLE_COLORS[u.role]||'#888')+'22', color: ROLE_COLORS[u.role]||'#888' }}>{u.role}</span></td>
              <td style={s.td}>{u.joined}</td>
              <td style={s.td}><span style={{ ...s.badge, background: u.status==='Active' ? '#e8f5e9' : '#ffebee', color: u.status==='Active' ? '#2e7d32' : '#c62828' }}>{u.status}</span></td>
              <td style={s.td}><button onClick={() => toggle(u.id)} style={{ background: u.status==='Active' ? '#ffebee' : '#e8f5e9', color: u.status==='Active' ? '#c62828' : '#2e7d32', border: 'none', borderRadius: 6, padding: '4px 12px', cursor: 'pointer', fontWeight: 600 }}>{u.status==='Active' ? 'Block' : 'Unblock'}</button></td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </div>
  );
}
