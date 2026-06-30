import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { fetchCart, updateCartItem, removeFromCart, checkout, resetCheckout } from '../../redux/cartSlice';

export default function Cart() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { items, loading, error, checkoutSuccess } = useSelector((s) => s.cart);
  const [address, setAddress] = useState('');
  const [showCheckout, setShowCheckout] = useState(false);

  useEffect(() => { dispatch(fetchCart()); }, [dispatch]);

  useEffect(() => {
    if (checkoutSuccess) {
      dispatch(resetCheckout());
      navigate('/customer/orders');
    }
  }, [checkoutSuccess, dispatch, navigate]);

  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  const handleQty = (item, delta) => {
    const newQty = item.quantity + delta;
    if (newQty < 1) { dispatch(removeFromCart(item.id)); return; }
    dispatch(updateCartItem({ id: item.id, data: { quantity: newQty } }));
  };

  const handleCheckout = (e) => {
    e.preventDefault();
    dispatch(checkout({ deliveryAddress: address }));
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>🛒 My Cart</h2>
        <ErrorMsg msg={error} />
        {loading ? <Loader /> : items.length === 0 ? (
          <p style={{ color: '#888' }}>Your cart is empty.</p>
        ) : (
          <div style={styles.layout}>
            <div style={styles.list}>
              {items.map(item => (
                <div key={item.id} style={styles.item}>
                  <div style={{ flex: 1 }}>
                    <div style={styles.iname}>{item.productName}</div>
                    <div style={styles.imeta}>{item.shopName}</div>
                    <div style={styles.iprice}>₹{item.price} × {item.quantity} = ₹{item.price * item.quantity}</div>
                  </div>
                  <div style={styles.qtyBox}>
                    <button style={styles.qBtn} onClick={() => handleQty(item, -1)}>−</button>
                    <span style={styles.qty}>{item.quantity}</span>
                    <button style={styles.qBtn} onClick={() => handleQty(item, 1)}>+</button>
                  </div>
                  <button style={styles.del} onClick={() => dispatch(removeFromCart(item.id))}>🗑</button>
                </div>
              ))}
            </div>
            <div style={styles.summary}>
              <h3 style={{ marginBottom: 16 }}>Order Summary</h3>
              <div style={styles.row}><span>Subtotal</span><span>₹{total}</span></div>
              <div style={styles.row}><span>Delivery</span><span>₹40</span></div>
              <div style={{ ...styles.row, fontWeight: 700, fontSize: 16, borderTop: '1px solid #eee', paddingTop: 12, marginTop: 8 }}>
                <span>Total</span><span style={{ color: '#e94560' }}>₹{total + 40}</span>
              </div>
              <button style={styles.coBtn} onClick={() => setShowCheckout(true)}>Proceed to Checkout</button>
            </div>
          </div>
        )}
        {showCheckout && (
          <div style={styles.modal}>
            <div style={styles.modalBox}>
              <h3>Delivery Details</h3>
              <form onSubmit={handleCheckout}>
                <textarea style={styles.textarea} placeholder="Enter delivery address..."
                  value={address} onChange={e => setAddress(e.target.value)} required rows={3} />
                <div style={{ display: 'flex', gap: 10, marginTop: 12 }}>
                  <button type="submit" style={styles.coBtn} disabled={loading}>
                    {loading ? 'Placing...' : '✅ Place Order'}
                  </button>
                  <button type="button" style={styles.cancelBtn} onClick={() => setShowCheckout(false)}>Cancel</button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 20 },
  layout: { display: 'flex', gap: 24, flexWrap: 'wrap' },
  list: { flex: 2, minWidth: 300 },
  item: { display: 'flex', alignItems: 'center', gap: 12, background: '#fff', borderRadius: 10, padding: 14, marginBottom: 10, boxShadow: '0 1px 4px rgba(0,0,0,0.07)' },
  iname: { fontWeight: 600, fontSize: 15 },
  imeta: { color: '#888', fontSize: 12 },
  iprice: { color: '#e94560', fontSize: 14, marginTop: 4 },
  qtyBox: { display: 'flex', alignItems: 'center', gap: 8 },
  qBtn: { width: 28, height: 28, border: '1px solid #ddd', borderRadius: 6, cursor: 'pointer', fontSize: 16, background: '#fff' },
  qty: { fontWeight: 600, fontSize: 16 },
  del: { background: 'none', border: 'none', cursor: 'pointer', fontSize: 18 },
  summary: { flex: 1, minWidth: 220, background: '#fff', borderRadius: 12, padding: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.08)', height: 'fit-content' },
  row: { display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: 14 },
  coBtn: { width: '100%', padding: '12px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600, marginTop: 12 },
  cancelBtn: { width: '100%', padding: '12px', background: '#ddd', color: '#333', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600 },
  modal: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modalBox: { background: '#fff', borderRadius: 12, padding: 28, width: 400, maxWidth: '90vw' },
  textarea: { width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, boxSizing: 'border-box', resize: 'vertical' },
};
