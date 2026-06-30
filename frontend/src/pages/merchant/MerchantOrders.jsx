import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const INIT_ORDERS = [
  { id: 'NK1001', customer: 'Ravi Kumar', items: 'Rice x2, Dal x1', total: 620, status: 'Pending' },
  { id: 'NK1002', customer: 'Priya Sharma', items: 'Oil x1, Sugar x2', total: 270, status: 'Confirmed' },
  { id: 'NK1003', customer: 'Suresh Babu', items: 'Eggs x1, Milk x3', total: 156, status: 'Out for Delivery' },
  { id: 'NK1004', customer: 'Anita Rao', items: 'Rice x1', total: 250, status: 'Delivered' },
];

const STATUS_COLORS = { Pending:'#e65100', Confirmed:'#1565c0', 'Out for Delivery':'#7b1fa2', Delivered:'#2e7d32' };

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 900, margin: '0 auto', padding: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 20, marginBottom: 14, boxShadow: '0 2px 8px rgba(0,0,0,.07)' },
  row: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 },
  badge: { borderRadius: 20, padding: '3px 12px', fontSize: 12, fontWeight: 700 },
  select: { padding: '6px 10px', border: '1px solid #ddd', borderRadius: 8, fontSize: 13 },
};

export default function MerchantOrders() {
  const [orders, setOrders] = useState(INIT_ORDERS);
  const updateStatus = (id, status) => setOrders(o => o.map(x => x.id === id ? { ...x, status } : x));
  return (
    <div style={s.page}>
      <Navbar role="MERCHANT" />
      <div style={s.container}>
        <h2>📦 Orders</h2>
        {orders.map(o => (
          <div key={o.id} style={s.card}>
            <div style={s.row}>
              <span style={{ fontWeight: 700, color: '#1a73e8' }}>#{o.id}</span>
              <span style={{ ...s.badge, background: (STATUS_COLORS[o.status]||'#888')+'22', color: STATUS_COLORS[o.status]||'#888' }}>{o.status}</span>
            </div>
            <div><strong>Customer:</strong> {o.customer}</div>
            <div style={{ color: '#666', fontSize: 13, margin: '4px 0' }}>{o.items}</div>
            <div style={s.row}>
              <span style={{ fontWeight: 700 }}>₹{o.total}</span>
              <select style={s.select} value={o.status} onChange={e => updateStatus(o.id, e.target.value)}>
                {['Pending','Confirmed','Out for Delivery','Delivered'].map(st => <option key={st}>{st}</option>)}
              </select>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
