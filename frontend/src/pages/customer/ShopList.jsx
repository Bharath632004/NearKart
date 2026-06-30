import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';

const styles = {
  page: { minHeight: '100vh', background: '#f5f7fa' },
  header: { padding: '24px 32px', fontSize: 22, fontWeight: 700 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(240px,1fr))', gap: 20, padding: '0 32px 32px' },
  card: { background: '#fff', borderRadius: 12, padding: 20, boxShadow: '0 2px 10px rgba(0,0,0,.07)', textDecoration: 'none', color: '#222' },
  shopIcon: { fontSize: 42, marginBottom: 10 },
  name: { fontWeight: 700, fontSize: 16 },
  meta: { color: '#666', fontSize: 13, marginTop: 4 },
  badge: { display: 'inline-block', background: '#e8f5e9', color: '#2e7d32', borderRadius: 12, padding: '2px 10px', fontSize: 12, marginTop: 8 },
};

const MOCK_SHOPS = [
  { id: 1, name: 'Sri Lakshmi Grocery', category: 'Grocery', distance: '0.4 km', rating: 4.5 },
  { id: 2, name: 'Raju Medical Store', category: 'Pharmacy', distance: '0.8 km', rating: 4.2 },
  { id: 3, name: 'Fresh Vegetables & Fruits', category: 'Vegetables', distance: '1.1 km', rating: 4.7 },
  { id: 4, name: 'Annapurna Bakery', category: 'Bakery', distance: '1.4 km', rating: 4.3 },
  { id: 5, name: 'Mobile World', category: 'Electronics', distance: '1.9 km', rating: 4.0 },
  { id: 6, name: 'Karthik Stationery', category: 'Stationery', distance: '2.2 km', rating: 4.1 },
];

export default function ShopList() {
  const [shops, setShops] = useState([]);
  const [search, setSearch] = useState('');
  useEffect(() => { setShops(MOCK_SHOPS); }, []);

  // fix: renamed filter param from 's' to 'shop' to avoid shadowing the styles object
  const filtered = shops.filter(shop =>
    shop.name.toLowerCase().includes(search.toLowerCase()) ||
    shop.category.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div style={styles.page}>
      <Navbar role="CUSTOMER" />
      <div style={{ padding: '24px 32px' }}>
        <h2 style={{ margin: 0 }}>🏪 Shops Near You</h2>
        <input
          placeholder="Search shops or category..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ marginTop: 16, padding: '10px 16px', width: '100%', maxWidth: 400, borderRadius: 8, border: '1px solid #ddd', fontSize: 14 }}
        />
      </div>
      <div style={styles.grid}>
        {filtered.map(shop => (
          <Link key={shop.id} to={`/customer/shops/${shop.id}/products`} style={styles.card}>
            <div style={styles.shopIcon}>🏪</div>
            <div style={styles.name}>{shop.name}</div>
            <div style={styles.meta}>{shop.category} · {shop.distance}</div>
            <span style={styles.badge}>⭐ {shop.rating}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
