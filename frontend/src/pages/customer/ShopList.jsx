import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getShopsApi } from '../../api/shopApi';

export default function ShopList() {
  const [shops, setShops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    getShopsApi().then(r => setShops(r.data || []))
      .catch(() => setError('Failed to load shops'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = shops.filter(s =>
    s.name?.toLowerCase().includes(search.toLowerCase()) ||
    s.category?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>🏪 Nearby Shops</h2>
        <input style={styles.search} placeholder="Search shops or category..."
          value={search} onChange={e => setSearch(e.target.value)} />
        <ErrorMsg msg={error} />
        {loading ? <Loader /> : (
          filtered.length === 0 ? <p style={{ color: '#888' }}>No shops found.</p> : (
            <div style={styles.grid}>
              {filtered.map(shop => (
                <Link to={`/customer/shops/${shop.id}/products`} key={shop.id} style={styles.card}>
                  <div style={styles.shopIcon}>🏬</div>
                  <div style={styles.shopName}>{shop.name}</div>
                  <div style={styles.shopMeta}>{shop.category || 'General'}</div>
                  <div style={styles.shopMeta}>📍 {shop.address || 'Nearby'}</div>
                  <div style={{ ...styles.badge, background: shop.isOpen ? '#22c55e' : '#ef4444' }}>
                    {shop.isOpen ? 'Open' : 'Closed'}
                  </div>
                </Link>
              ))}
            </div>
          )
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 16 },
  search: { width: '100%', maxWidth: 400, padding: '10px 14px', border: '1px solid #ddd', borderRadius: 8, marginBottom: 20, fontSize: 14, boxSizing: 'border-box' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 16 },
  card: { background: '#fff', borderRadius: 12, padding: 20, textDecoration: 'none', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', transition: 'transform 0.2s', display: 'block' },
  shopIcon: { fontSize: 40, textAlign: 'center', marginBottom: 8 },
  shopName: { fontWeight: 700, fontSize: 16, color: '#1a1a2e', textAlign: 'center' },
  shopMeta: { color: '#666', fontSize: 13, textAlign: 'center', marginTop: 4 },
  badge: { borderRadius: 12, padding: '2px 10px', color: '#fff', fontSize: 11, textAlign: 'center', marginTop: 8, display: 'inline-block' },
};
