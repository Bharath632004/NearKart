import axios from 'axios';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token from localStorage on every request
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    try {
      const raw = localStorage.getItem('nearkart-auth');
      if (raw) {
        const parsed = JSON.parse(raw);
        const token = parsed?.state?.token;
        if (token) config.headers.Authorization = `Bearer ${token}`;
      }
    } catch {}
  }
  return config;
});

// ─── Auth (/api/v1/auth) ──────────────────────────────────────────────────────
export const authApi = {
  login: (email: string, password: string) =>
    api.post('/api/v1/auth/login', { email, password }),
  register: (data: { name: string; email: string; password: string; phone?: string }) =>
    api.post('/api/v1/auth/register', data),
  forgotPassword: (email: string) =>
    api.post('/api/v1/auth/forgot-password', { email }),
  resetPassword: (token: string, password: string) =>
    api.post('/api/v1/auth/reset-password', { token, password }),
};

// ─── Users (/api/users, /api/v1/users) ───────────────────────────────────────
export const usersApi = {
  getAll: () => api.get('/api/users'),
  getMe: () => api.get('/api/v1/users/me'),
  updateMe: (data: object) => api.put('/api/v1/users/me', data),
  getAddresses: () => api.get('/api/users/me/addresses'),
  addAddress: (data: object) => api.post('/api/users/me/addresses', data),
  deleteAddress: (id: number) => api.delete(`/api/users/me/addresses/${id}`),
};

// ─── Products (/api/products) ─────────────────────────────────────────────────
export const productsApi = {
  getAll: (params?: { category?: string; search?: string; page?: number }) =>
    api.get('/api/products', { params }),
  getById: (id: number) => api.get(`/api/products/${id}`),
  create: (data: object) => api.post('/api/products', data),
  update: (id: number, data: object) => api.put(`/api/products/${id}`, data),
  delete: (id: number) => api.delete(`/api/products/${id}`),
};

// ─── Orders (/api/orders) ─────────────────────────────────────────────────────
export const ordersApi = {
  create: (data: { items: { productId: number; qty: number }[]; address: string }) =>
    api.post('/api/orders', data),
  getAll: () => api.get('/api/orders'),
  getById: (id: number) => api.get(`/api/orders/${id}`),
  cancel: (id: number) => api.patch(`/api/orders/${id}/cancel`),
};

// ─── Cart (/api/v1/cart) ──────────────────────────────────────────────────────
export const cartApi = {
  get: () => api.get('/api/v1/cart'),
  addItem: (data: { productId: number; qty: number }) =>
    api.post('/api/v1/cart/items', data),
  updateItem: (itemId: number, qty: number) =>
    api.put(`/api/v1/cart/items/${itemId}`, { qty }),
  removeItem: (itemId: number) => api.delete(`/api/v1/cart/items/${itemId}`),
  clear: () => api.delete('/api/v1/cart'),
};

// ─── Shops (/api/shops) ───────────────────────────────────────────────────────
export const shopsApi = {
  getAll: (params?: { city?: string; search?: string }) =>
    api.get('/api/shops', { params }),
  getById: (id: number) => api.get(`/api/shops/${id}`),
  getNearby: (lat: number, lng: number, radius?: number) =>
    api.get('/api/shops/nearby', { params: { lat, lng, radius } }),
};

// ─── Merchants (/api/v1/merchants) ───────────────────────────────────────────
export const merchantsApi = {
  register: (data: object) => api.post('/api/v1/merchants/register', data),
  getProfile: () => api.get('/api/v1/merchants/me'),
  updateProfile: (data: object) => api.put('/api/v1/merchants/me', data),
  getSettlements: () => api.get('/api/v1/settlements'),
  getSettlementById: (id: string) => api.get(`/api/v1/settlements/${id}`),
};

// ─── Payments (/api/v1/payments, /api/v1/wallet, /api/v1/refunds) ────────────
export const paymentsApi = {
  initiate: (data: { orderId: string; method: string; amount: number }) =>
    api.post('/api/v1/payments', data),
  getById: (id: string) => api.get(`/api/v1/payments/${id}`),
  getByOrder: (orderId: string) =>
    api.get('/api/v1/payments', { params: { orderId } }),
  getWallet: () => api.get('/api/v1/wallet'),
  addWalletFunds: (amount: number) =>
    api.post('/api/v1/wallet/topup', { amount }),
  requestRefund: (data: { paymentId: string; reason: string }) =>
    api.post('/api/v1/refunds', data),
  getRefund: (id: string) => api.get(`/api/v1/refunds/${id}`),
};

// ─── Delivery (/api/v1/delivery) ──────────────────────────────────────────────
export const deliveryApi = {
  trackOrder: (orderId: string) =>
    api.get(`/api/v1/delivery/tracking/${orderId}`),
  getAssignment: (assignmentId: string) =>
    api.get(`/api/v1/delivery/assignments/${assignmentId}`),
  getPartnerProfile: () => api.get('/api/v1/delivery/partners/me'),
  updatePartnerLocation: (data: { lat: number; lng: number }) =>
    api.patch('/api/v1/delivery/partners/me/location', data),
  getEarnings: () => api.get('/api/v1/delivery/earnings'),
};

// ─── Notifications (/api/v1/notifications) ───────────────────────────────────
export const notificationsApi = {
  getAll: () => api.get('/api/v1/notifications'),
  markRead: (id: string) =>
    api.patch(`/api/v1/notifications/${id}/read`),
  markAllRead: () => api.patch('/api/v1/notifications/read-all'),
};

// ─── Analytics (/api/analytics) ───────────────────────────────────────────────
export const analyticsApi = {
  getDashboard: () => api.get('/api/analytics/dashboard'),
  getSales: (params?: { from?: string; to?: string }) =>
    api.get('/api/analytics/sales', { params }),
  getMerchantAnalytics: (params?: { merchantId?: string }) =>
    api.get('/api/analytics/merchants', { params }),
  getCustomerAnalytics: () => api.get('/api/analytics/customers'),
  getDeliveryAnalytics: () => api.get('/api/analytics/delivery'),
};

// ─── Admin (/api/admin) ───────────────────────────────────────────────────────
export const adminApi = {
  getDashboard: () => api.get('/api/admin/dashboard'),
  getUsers: () => api.get('/api/admin/users'),
  updateUser: (id: string, data: object) =>
    api.put(`/api/admin/users/${id}`, data),
  getMerchants: () => api.get('/api/admin/merchants'),
  approveMerchant: (id: string) =>
    api.patch(`/api/admin/merchants/${id}/approve`),
  getCoupons: () => api.get('/api/admin/coupons'),
  createCoupon: (data: object) => api.post('/api/admin/coupons', data),
  deleteCoupon: (id: string) => api.delete(`/api/admin/coupons/${id}`),
  getAuditLogs: () => api.get('/api/admin/audit'),
};

export default api;
