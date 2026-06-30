import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';

const navStyles = {
  navbar: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 32px', background: '#1a73e8', color: '#fff' },
  brand: { fontWeight: 700, fontSize: 22, color: '#fff', textDecoration: 'none' },
  navLinks: { display: 'flex', gap: 20, listStyle: 'none', margin: 0, padding: 0 },
  link: { color: '#fff', textDecoration: 'none', fontWeight: 500 },
  btn: { background: '#fff', color: '#1a73e8', border: 'none', borderRadius: 6, padding: '6px 18px', cursor: 'pointer', fontWeight: 600 }
};

export default function Navbar({ role }) {
  const navigate = useNavigate();
  // fix: use useLocation for active link highlighting
  const { pathname } = useLocation();

  const logout = () => {
    localStorage.removeItem('nearkart_token');
    localStorage.removeItem('nearkart_role');
    navigate('/login');
  };

  const links = {
    CUSTOMER: [
      { to: '/customer', label: 'Home' },
      { to: '/customer/shops', label: 'Shops' },
      { to: '/customer/cart', label: '🛒 Cart' },
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
    <nav style={navStyles.navbar}>
      <Link to="/" style={navStyles.brand}>🛒 NearKart</Link>
      <ul style={navStyles.navLinks}>
        {(links[role] || []).map(l => (
          <li key={l.to}>
            {/* fix: active link gets underline indicator */}
            <Link
              to={l.to}
              style={{
                ...navStyles.link,
                borderBottom: pathname === l.to ? '2px solid #fff' : '2px solid transparent',
                paddingBottom: 3,
              }}
            >
              {l.label}
            </Link>
          </li>
        ))}
      </ul>
      <button style={navStyles.btn} onClick={logout}>Logout</button>
    </nav>
  );
}
