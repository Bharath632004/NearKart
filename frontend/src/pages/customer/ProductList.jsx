import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import Navbar from '../../components/Navbar';

const MOCK_PRODUCTS = [
  { id: 1, name: 'Rice (5 kg)', price: 250, unit: 'bag', emoji: '🌾' },
  { id: 2, name: 'Toor Dal (1 kg)', price: 120, unit: 'kg', emoji: '🫘' },
  { id: 3, name: 'Sunflower Oil (1 L)', price: 180, unit: 'bottle', emoji: '🫙' },
  { id: 4, name: 'Sugar (1 kg)', price: 45, unit: 'kg', emoji: '🍬' },
  { id: 5, name: 'Milk (500 ml)', price: 28, unit: 'packet', emoji: '🥛' },
  { id: 6, name: 'Eggs (12 pcs)', price: 72, unit: 'dozen', emoji: '🥚' },
];

export default function ProductList() {
  const { shopId } = useParams();
  const [cart, setCart] = useState({});

  // fix: persist cart to localStorage so Cart.jsx can read it
  const addToCart = (product) => {
    setCart(prev => {
      const updated = { ...prev, [product.id]: (prev[product.id] || 0) + 1 };
      localStorage.setItem('nearkart_cart', JSON.stringify(updated));
      return updated;
    });
  };

  const cartCount = Object.values(cart).reduce((a, b) => a + b, 0);

  const s = {
    page: { minHeight: '100vh', background: '#f5f7fa' },
    header: { padding: '24px 32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
    grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(200px,1fr))', gap: 20, padding: '0 32px 32px' },
    card: { background: '#fff', borderRadius: 12, padding: 20, boxShadow: '0 2px 10px rgba(0,0,0,.07)', textAlign: 'center' },
    emoji: { fontSize: 40 },
    name: { fontWeight: 600, marginTop: 8, fontSize: 15 },
    price: { color: '#1a73e8', fontWeight: 700, fontSize: 16, margin: '8px 0' },
    btn: { background: '#1a73e8', color: '#fff', border: 'none', borderRadius: 8, padding: '8px 20px', cursor: 'pointer', fontWeight: 600 },
    cartBadge: { background: '#1a73e8', color: '#fff', borderRadius: 8, padding: '8px 20px', fontWeight: 600 },
  };

  return (
    <div style={s.page}>
      <Navbar role="CUSTOMER" />
      <div style={s.header}>
        <h2 style={{ margin: 0 }}>🛍️ Products (Shop #{shopId})</h2>
        <span style={s.cartBadge}>🛒 Cart: {cartCount} items</span>
      </div>
      <div style={s.grid}>
        {MOCK_PRODUCTS.map(product => (
          <div key={product.id} style={s.card}>
            <div style={s.emoji}>{product.emoji}</div>
            <div style={s.name}>{product.name}</div>
            <div style={s.price}>₹{product.price} / {product.unit}</div>
            {cart[product.id] ? (
              <span style={{ fontWeight: 600, color: '#2e7d32' }}>✅ {cart[product.id]} added</span>
            ) : (
              <button style={s.btn} onClick={() => addToCart(product)}>Add to Cart</button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
