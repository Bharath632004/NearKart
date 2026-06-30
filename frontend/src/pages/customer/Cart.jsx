import React, { useState } from 'react';
import Navbar from '../../components/Navbar';

const MOCK_PRODUCTS = [
  { id: 1, name: 'Rice (5 kg)', price: 250, emoji: '🌾' },
  { id: 2, name: 'Toor Dal (1 kg)', price: 120, emoji: '🫘' },
  { id: 3, name: 'Sunflower Oil (1 L)', price: 180, emoji: '🫙' },
  { id: 4, name: 'Sugar (1 kg)', price: 45, emoji: '🍬' },
  { id: 5, name: 'Milk (500 ml)', price: 28, emoji: '🥛' },
  { id: 6, name: 'Eggs (12 pcs)', price: 72, emoji: '🥚' },
];

// fix: Read cart from localStorage (persisted by ProductList) instead of hardcoded MOCK_CART
function buildCartItems(cartMap) {
  return Object.entries(cartMap)
    .map(([id, qty]) => {
      const product = MOCK_PRODUCTS.find(p => p.id === Number(id));
      if (!product) return null;
      return { ...product, qty };
    })
    .filter(Boolean);
}

const s = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  container: { maxWidth: 700, margin: '0 auto', padding: 32 },
  card: { background: '#fff', borderRadius: 12, padding: 20, boxShadow: '0 2px 10px rgba(0,0,0,.07)', marginBottom: 16, display: 'flex', alignItems: 'center', gap: 16 },
  emoji: { fontSize: 36 },
  name: { fontWeight: 600, flex: 1 },
  price: { color: '#1a73e8', fontWeight: 700 },
  summary: { background: '#fff', borderRadius: 12, padding: 24, boxShadow: '0 2px 10px rgba(0,0,0,.07)', marginTop: 16 },
  orderBtn: { background: '#1a73e8', color: '#fff', border: 'none', borderRadius: 8, padding: '14px 32px', fontSize: 16, cursor: 'pointer', fontWeight: 700, width: '100%', marginTop: 16 },
};

export default function Cart() {
  const [items, setItems] = useState(() => {
    const saved = localStorage.getItem('nearkart_cart');
    if (saved) {
      const parsed = JSON.parse(saved);
      return buildCartItems(parsed);
    }
    // fallback default items if nothing in localStorage
    return [
      { id: 1, name: 'Rice (5 kg)', price: 250, qty: 2, emoji: '🌾' },
      { id: 2, name: 'Toor Dal (1 kg)', price: 120, qty: 1, emoji: '🫘' },
      { id: 3, name: 'Sunflower Oil (1 L)', price: 180, qty: 1, emoji: '🫙' },
    ];
  });

  const total = items.reduce((sum, i) => sum + i.price * i.qty, 0);

  const remove = (id) => {
    setItems(prev => {
      const updated = prev.filter(i => i.id !== id);
      const cartMap = {};
      updated.forEach(i => { cartMap[i.id] = i.qty; });
      localStorage.setItem('nearkart_cart', JSON.stringify(cartMap));
      return updated;
    });
  };

  const [ordered, setOrdered] = useState(false);

  const placeOrder = () => {
    localStorage.removeItem('nearkart_cart');
    setOrdered(true);
  };

  return (
    <div style={s.page}>
      <Navbar role="CUSTOMER" />
      <div style={s.container}>
        <h2>🛒 My Cart ({items.length} items)</h2>
        {ordered ? (
          <div style={{ ...s.summary, textAlign: 'center', color: '#2e7d32' }}>
            <p style={{ fontSize: 48 }}>✅</p>
            <h3>Order Placed Successfully!</h3>
            <p>Your order is being confirmed by the merchant.</p>
          </div>
        ) : (
          <>
            {items.map(item => (
              <div key={item.id} style={s.card}>
                <span style={s.emoji}>{item.emoji}</span>
                <span style={s.name}>{item.name}</span>
                <span style={{ color: '#666', marginRight: 8 }}>x{item.qty}</span>
                <span style={s.price}>₹{item.price * item.qty}</span>
                <button onClick={() => remove(item.id)} style={{ background: '#ffebee', color: '#c62828', border: 'none', borderRadius: 6, padding: '4px 10px', cursor: 'pointer' }}>✕</button>
              </div>
            ))}
            <div style={s.summary}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: 18 }}>
                <span>Total</span><span>₹{total}</span>
              </div>
              <button style={s.orderBtn} onClick={placeOrder}>Place Order 🚀</button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
