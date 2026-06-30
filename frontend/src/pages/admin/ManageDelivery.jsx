import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const PARTNERS = [
  { id: 1, name: 'Ramesh Delivery', phone: '9876543211', area: 'Vanikamdinne', deliveries: 128, status: 'Online', rating: 4.8 },
  { id: 2, name: 'Kumar Reddy', phone: '9876543212', area: 'Gandhi Nagar', deliveries: 95, status: 'Offline', rating: 4.5 },
  { id: 3, name: 'Anil Kumar', phone: '9876543213', area: 'Market Area', deliveries: 210, status: 'Online', rating: 4.9 },
  { id: 4, name: 'Siva Prasad', phone: '9876543214', area: 'Station Road', deliveries: 67, status: 'Online', rating: 4.3 },
];

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 950, margin: '0 auto', padding: 32 },
  table: { width: '100%', background: '#fff', borderRadius: 12, overflow: 'hidden', boxShadow: '0 2px 10px rgba(0,0,0,.07)', borderCollapse: 'collapse' },
  th: { background: '#e65100', color: '#fff', padding: '12px 16px', textAlign: 'left', fontWeight: 600 },
  td: { padding: '12px 16px', borderBottom: '1px solid #f0f0f0' },
  badge: { borderRadius: 12, padding: '2px 10px', fontSize: 12, fontWeight: 600 },
};

export default function ManageDelivery() {
  const [partners, setPartners] = useState(PARTNERS);
  const remove = (id) => setPartners(p => p.filter(x => x.id !== id));
  return (
    <div style={s.page}>
      <Navbar role="ADMIN" />
      <div style={s.container}>
        <h2>🚴 Delivery Partners</h2>
        <table style={s.table}>
          <thead><tr>{['Name','Phone','Area','Deliveries','Rating','Status','Action'].map(h => <th key={h} style={s.th}>{h}</th>)}</tr></thead>
          <tbody>{partners.map(p => (
            <tr key={p.id}>
              <td style={s.td}>{p.name}</td>
              <td style={s.td}>{p.phone}</td>
              <td style={s.td}>{p.area}</td>
              <td style={s.td}>{p.deliveries}</td>
              <td style={s.td}>⭐ {p.rating}</td>
              <td style={s.td}><span style={{ ...s.badge, background: p.status==='Online' ? '#e8f5e9' : '#f5f5f5', color: p.status==='Online' ? '#2e7d32' : '#888' }}>{p.status}</span></td>
              <td style={s.td}><button onClick={() => remove(p.id)} style={{ background: '#ffebee', color: '#c62828', border: 'none', borderRadius: 6, padding: '4px 12px', cursor: 'pointer' }}>Remove</button></td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </div>
  );
}
