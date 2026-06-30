import React from 'react';
import Navbar from '../../components/Navbar';

const HISTORY = [
  { id: 'NK0990', customer: 'Arun Reddy', address: 'Bus Stand Road', date: '2026-06-29', earnings: 60, rating: 5 },
  { id: 'NK0985', customer: 'Lata Patel', address: 'Near HDFC Bank', date: '2026-06-28', earnings: 55, rating: 4 },
  { id: 'NK0971', customer: 'Mohan Das', address: 'Station Road', date: '2026-06-27', earnings: 70, rating: 5 },
  { id: 'NK0965', customer: 'Sunita Rao', address: 'Old Post Office Lane', date: '2026-06-26', earnings: 50, rating: 4 },
];

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 750, margin: '0 auto', padding: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 18, marginBottom: 12, boxShadow: '0 2px 8px rgba(0,0,0,.07)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
};

export default function DeliveryHistory() {
  const totalEarnings = HISTORY.reduce((a, b) => a + b.earnings, 0);
  return (
    <div style={s.page}>
      <Navbar role="DELIVERY" />
      <div style={s.container}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2>📋 Delivery History</h2>
          <span style={{ background: '#e8f5e9', color: '#2e7d32', borderRadius: 8, padding: '6px 16px', fontWeight: 700 }}>Total: ₹{totalEarnings}</span>
        </div>
        {HISTORY.map(h => (
          <div key={h.id} style={s.card}>
            <div>
              <span style={{ fontWeight: 700, color: '#1a73e8' }}>#{h.id}</span>
              <div style={{ fontWeight: 600 }}>{h.customer}</div>
              <div style={{ color: '#888', fontSize: 13 }}>{h.address} · {h.date}</div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontWeight: 700, color: '#2e7d32' }}>₹{h.earnings}</div>
              <div>{'⭐'.repeat(h.rating)}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
