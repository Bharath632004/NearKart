import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './redux/store';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

// Customer Panel
import CustomerDashboard from './pages/customer/CustomerDashboard';
import ShopList from './pages/customer/ShopList';
import ProductList from './pages/customer/ProductList';
import Cart from './pages/customer/Cart';
import OrderHistory from './pages/customer/OrderHistory';
import CustomerProfile from './pages/customer/CustomerProfile';

// Merchant Panel
import MerchantDashboard from './pages/merchant/MerchantDashboard';
import ManageProducts from './pages/merchant/ManageProducts';
import MerchantOrders from './pages/merchant/MerchantOrders';
import MerchantProfile from './pages/merchant/MerchantProfile';

// Delivery Panel
import DeliveryDashboard from './pages/delivery/DeliveryDashboard';
import ActiveDeliveries from './pages/delivery/ActiveDeliveries';
import DeliveryHistory from './pages/delivery/DeliveryHistory';
import DeliveryProfile from './pages/delivery/DeliveryProfile';

// Admin Panel
import AdminDashboard from './pages/admin/AdminDashboard';
import ManageUsers from './pages/admin/ManageUsers';
import ManageMerchants from './pages/admin/ManageMerchants';
import ManageDelivery from './pages/admin/ManageDelivery';
import AdminReports from './pages/admin/AdminReports';

// fix: Role-based redirect from root instead of always going to /login
function RootRedirect() {
  const token = localStorage.getItem('nearkart_token');
  const role = localStorage.getItem('nearkart_role');
  if (!token) return <Navigate to="/login" />;
  const roleMap = { CUSTOMER: '/customer', MERCHANT: '/merchant', DELIVERY: '/delivery', ADMIN: '/admin' };
  return <Navigate to={roleMap[role] || '/login'} />;
}

function PrivateRoute({ children, role }) {
  const token = localStorage.getItem('nearkart_token');
  const userRole = localStorage.getItem('nearkart_role');
  if (!token) return <Navigate to="/login" />;
  if (role && userRole !== role) return <Navigate to="/login" />;
  return children;
}

export default function App() {
  return (
    <Provider store={store}>
      <Router>
        <Routes>
          {/* Auth */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          {/* fix: Smart role-based root redirect */}
          <Route path="/" element={<RootRedirect />} />

          {/* Customer Routes */}
          <Route path="/customer" element={<PrivateRoute role="CUSTOMER"><CustomerDashboard /></PrivateRoute>} />
          <Route path="/customer/shops" element={<PrivateRoute role="CUSTOMER"><ShopList /></PrivateRoute>} />
          <Route path="/customer/shops/:shopId/products" element={<PrivateRoute role="CUSTOMER"><ProductList /></PrivateRoute>} />
          <Route path="/customer/cart" element={<PrivateRoute role="CUSTOMER"><Cart /></PrivateRoute>} />
          <Route path="/customer/orders" element={<PrivateRoute role="CUSTOMER"><OrderHistory /></PrivateRoute>} />
          <Route path="/customer/profile" element={<PrivateRoute role="CUSTOMER"><CustomerProfile /></PrivateRoute>} />

          {/* Merchant Routes */}
          <Route path="/merchant" element={<PrivateRoute role="MERCHANT"><MerchantDashboard /></PrivateRoute>} />
          <Route path="/merchant/products" element={<PrivateRoute role="MERCHANT"><ManageProducts /></PrivateRoute>} />
          <Route path="/merchant/orders" element={<PrivateRoute role="MERCHANT"><MerchantOrders /></PrivateRoute>} />
          <Route path="/merchant/profile" element={<PrivateRoute role="MERCHANT"><MerchantProfile /></PrivateRoute>} />

          {/* Delivery Routes */}
          <Route path="/delivery" element={<PrivateRoute role="DELIVERY"><DeliveryDashboard /></PrivateRoute>} />
          <Route path="/delivery/active" element={<PrivateRoute role="DELIVERY"><ActiveDeliveries /></PrivateRoute>} />
          <Route path="/delivery/history" element={<PrivateRoute role="DELIVERY"><DeliveryHistory /></PrivateRoute>} />
          <Route path="/delivery/profile" element={<PrivateRoute role="DELIVERY"><DeliveryProfile /></PrivateRoute>} />

          {/* Admin Routes */}
          <Route path="/admin" element={<PrivateRoute role="ADMIN"><AdminDashboard /></PrivateRoute>} />
          <Route path="/admin/users" element={<PrivateRoute role="ADMIN"><ManageUsers /></PrivateRoute>} />
          <Route path="/admin/merchants" element={<PrivateRoute role="ADMIN"><ManageMerchants /></PrivateRoute>} />
          <Route path="/admin/delivery" element={<PrivateRoute role="ADMIN"><ManageDelivery /></PrivateRoute>} />
          <Route path="/admin/reports" element={<PrivateRoute role="ADMIN"><AdminReports /></PrivateRoute>} />

          {/* fix: Catch-all 404 redirect */}
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </Router>
    </Provider>
  );
}
