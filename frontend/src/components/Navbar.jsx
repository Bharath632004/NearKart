import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../redux/authSlice';

export default function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { role } = useSelector((s) => s.auth);
  const cartItems = useSelector((s) => s.cart.items);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  const navLinks = {
    CUSTOMER: [
      { to: '/customer', label: 'Dashboard' },
      { to: '/customer/shops', label: 'Shops' },
      { to: '/customer/cart', label: `Cart (${cartItems.length})` },
      { to: '/customer/orders', label: 'Orders' },
      { to: '/customer/profile', label: 'Profile' },
    ],
    MERCHANT: [
      { to: '/merchant', label: 'Dashboard' },
      { to: '/merchant/products', label: 'Products' },
      { to: '/merchant/orders', label: 'Orders' },
      { to: '/merchant/profile', label: 'Profile' },
    ],
    DELIVERY: [
      { to: '/delivery', label: 'Dashboard' },
      { to: '/delivery/active', label: 'Active' },
      { to: '/delivery/history', label: 'History' },
      { to: '/delivery/profile', label: 'Profile' },
    ],
    ADMIN: [
      { to: '/admin', label: 'Dashboard' },
      { to: '/admin/users', label: 'Users' },
      { to: '/admin/merchants', label: 'Merchants' },
      { to: '/admin/delivery', label: 'Delivery' },
      { to: '/admin/reports', label: 'Reports' },
    ],
  };

  return (
    <nav style={styles.nav}>
      <Link to="/" style={styles.brand}>🛒 NearKart</Link>
      <div style={styles.links}>
        {(navLinks[role] || []).map((l) => (
          <Link key={l.to} to={l.to} style={styles.link}>{l.label}</Link>
        ))}
        {/* fix: only show Logout when a role exists (user is logged in) */}
        {role && <button onClick={handleLogout} style={styles.btn}>Logout</button>}
      </div>
    </nav>
  );
}

const styles = {
  nav: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 24px', background: '#1a1a2e', color: '#fff' },
  brand: { color: '#e94560', fontWeight: 700, fontSize: 20, textDecoration: 'none' },
  links: { display: 'flex', gap: 16, alignItems: 'center' },
  link: { color: '#eee', textDecoration: 'none', fontSize: 14 },
  btn: { background: '#e94560', color: '#fff', border: 'none', borderRadius: 6, padding: '6px 14px', cursor: 'pointer', fontSize: 14 },
};
