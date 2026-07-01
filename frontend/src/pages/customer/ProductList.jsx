import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getProductsByShopApi } from '../../api/productApi';
import { addToCart } from '../../redux/cartSlice';

export default function ProductList() {
  const { shopId } = useParams();
  const dispatch = useDispatch();
  const { loading: cartLoading } = useSelector((s) => s.cart);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [added, setAdded] = useState({});
  const [search, setSearch] = useState('');

  useEffect(() => {
    getProductsByShopApi(shopId).then(r => setProducts(r.data || []))
      .catch(() => setError('Failed to load products'))
      .finally(() => setLoading(false));
  }, [shopId]);

  // fix: only show success feedback if dispatch actually fulfilled; show error on failure
  const handleAdd = async (product) => {
    const res = await dispatch(addToCart({ productId: product.id, shopId, quantity: 1 }));
    if (addToCart.fulfilled.match(res)) {
      setAdded(a => ({ ...a, [product.id]: true }));
      setTimeout(() => setAdded(a => ({ ...a, [product.id]: false })), 2000);
    } else {
      setError('Failed to add to cart. Please try again.');
    }
  };

  const filtered = products.filter(p =>
    p.name?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>🛍️ Products</h2>
        <input style={styles.search} placeholder="Search products..."
          value={search} onChange={e => setSearch(e.target.value)} />
        <ErrorMsg msg={error} />
        {loading ? <Loader /> : (
          filtered.length === 0 ? <p style={{ color: '#888' }}>No products found.</p> : (
            <div style={styles.grid}>
              {filtered.map(p => (
                <div key={p.id} style={styles.card}>
                  <div style={styles.imgBox}>🥦</div>
                  <div style={styles.name}>{p.name}</div>
                  <div style={styles.desc}>{p.description || 'Fresh product'}</div>
                  <div style={styles.price}>₹{p.price}</div>
                  <div style={styles.stock}>{p.stock > 0 ? `In stock: ${p.stock}` : '❌ Out of stock'}</div>
                  <button
                    style={{ ...styles.btn, background: added[p.id] ? '#22c55e' : '#e94560', opacity: p.stock === 0 ? 0.5 : 1 }}
                    disabled={p.stock === 0 || cartLoading}
                    onClick={() => handleAdd(p)}
                  >
                    {added[p.id] ? '✅ Added!' : '🛒 Add to Cart'}
                  </button>
                </div>
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
  card: { background: '#fff', borderRadius: 12, padding: 16, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' },
  imgBox: { fontSize: 48, textAlign: 'center', marginBottom: 8 },
  name: { fontWeight: 700, fontSize: 15, color: '#1a1a2e' },
  desc: { color: '#888', fontSize: 12, marginTop: 4 },
  price: { color: '#e94560', fontWeight: 700, fontSize: 18, margin: '8px 0' },
  stock: { fontSize: 12, color: '#666', marginBottom: 10 },
  btn: { width: '100%', padding: '9px', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600 },
};
