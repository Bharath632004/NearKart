import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import Login from "./pages/Login";
import AdminLayout from "./components/AdminLayout";
import MerchantLayout from "./components/MerchantLayout";

import Dashboard from "./pages/admin/Dashboard";
import Users from "./pages/admin/Users";
import MerchantApproval from "./pages/admin/MerchantApproval";
import DeliveryPartners from "./pages/admin/DeliveryPartners";
import Orders from "./pages/admin/Orders";
import Payments from "./pages/admin/Payments";
import Coupons from "./pages/admin/Coupons";
import Analytics from "./pages/admin/Analytics";
import Settings from "./pages/admin/Settings";
import Reports from "./pages/admin/Reports";
import Notifications from "./pages/admin/Notifications";
import AuditLogs from "./pages/admin/AuditLogs";
import RolesPermissions from "./pages/admin/RolesPermissions";
import RevenueAnalytics from "./pages/admin/RevenueAnalytics";
import SecurityDashboard from "./pages/admin/SecurityDashboard";

import MerchantDashboard from "./pages/merchant/MerchantDashboard";
import Products from "./pages/merchant/Products";
import MerchantOrders from "./pages/merchant/MerchantOrders";
import SalesReport from "./pages/merchant/SalesReport";
import Settlements from "./pages/merchant/Settlements";

const ProtectedRoute = ({ children, role }) => {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" />;
  if (role && user.role !== role) return <Navigate to="/login" />;
  return children;
};

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />

          {/* Admin Routes */}
          <Route path="/admin" element={
            <ProtectedRoute role="admin"><AdminLayout /></ProtectedRoute>
          }>
            <Route index element={<Dashboard />} />
            <Route path="users" element={<Users />} />
            <Route path="merchants" element={<MerchantApproval />} />
            <Route path="delivery" element={<DeliveryPartners />} />
            <Route path="orders" element={<Orders />} />
            <Route path="payments" element={<Payments />} />
            <Route path="coupons" element={<Coupons />} />
            <Route path="analytics" element={<Analytics />} />
            <Route path="revenue" element={<RevenueAnalytics />} />
            <Route path="reports" element={<Reports />} />
            <Route path="notifications" element={<Notifications />} />
            <Route path="audit-logs" element={<AuditLogs />} />
            <Route path="roles" element={<RolesPermissions />} />
            <Route path="security" element={<SecurityDashboard />} />
            <Route path="settings" element={<Settings />} />
          </Route>

          {/* Merchant Routes */}
          <Route path="/merchant" element={
            <ProtectedRoute role="merchant"><MerchantLayout /></ProtectedRoute>
          }>
            <Route index element={<MerchantDashboard />} />
            <Route path="products" element={<Products />} />
            <Route path="orders" element={<MerchantOrders />} />
            <Route path="sales" element={<SalesReport />} />
            <Route path="settlements" element={<Settlements />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
