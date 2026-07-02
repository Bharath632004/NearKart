import API from "./axios";

// Dashboard
export const getDashboardStats = () => API.get("/admin/dashboard/stats");

// Analytics
export const getAnalytics = () => API.get("/admin/analytics");
export const getRevenueAnalytics = () => API.get("/admin/analytics/revenue");

// Users
export const getUsers = (params) => API.get("/admin/users", { params });
export const updateUser = (id, data) => API.put(`/admin/users/${id}`, data);
export const deleteUser = (id) => API.delete(`/admin/users/${id}`);
export const banUser = (id) => API.put(`/admin/users/${id}/ban`);

// Merchants
export const getMerchants = (params) => API.get("/admin/merchants", { params });
export const approveMerchant = (id) => API.put(`/admin/merchants/${id}/approve`);
export const rejectMerchant = (id) => API.put(`/admin/merchants/${id}/reject`);

// Delivery Partners
export const getDeliveryPartners = (params) => API.get("/admin/delivery-partners", { params });
export const updateDeliveryPartner = (id, data) => API.put(`/admin/delivery-partners/${id}`, data);

// Orders
export const getOrders = (params) => API.get("/admin/orders", { params });
export const updateOrderStatus = (id, status) => API.put(`/admin/orders/${id}/status`, { status });

// Payments
export const getPayments = (params) => API.get("/admin/payments", { params });
export const refundPayment = (id) => API.post(`/admin/payments/${id}/refund`);

// Coupons
export const getCoupons = () => API.get("/admin/coupons");
export const createCoupon = (data) => API.post("/admin/coupons", data);
export const updateCoupon = (id, data) => API.put(`/admin/coupons/${id}`, data);
export const deleteCoupon = (id) => API.delete(`/admin/coupons/${id}`);

// Reports
export const getReports = (type, params) => API.get(`/admin/reports/${type}`, { params });
export const exportReport = (type, format) => API.get(`/admin/reports/${type}/export`, { params: { format }, responseType: "blob" });

// Notifications
export const getNotifications = () => API.get("/admin/notifications");
export const sendNotification = (data) => API.post("/admin/notifications/send", data);
export const markNotificationRead = (id) => API.put(`/admin/notifications/${id}/read`);
export const markAllRead = () => API.put("/admin/notifications/read-all");

// Audit Logs
export const getAuditLogs = (params) => API.get("/admin/audit-logs", { params });

// Roles & Permissions
export const getRoles = () => API.get("/admin/roles");
export const createRole = (data) => API.post("/admin/roles", data);
export const updateRole = (id, data) => API.put(`/admin/roles/${id}`, data);
export const deleteRole = (id) => API.delete(`/admin/roles/${id}`);
export const getPermissions = () => API.get("/admin/permissions");

// Settings
export const getPlatformSettings = () => API.get("/admin/settings/platform");
export const updatePlatformSettings = (data) => API.put("/admin/settings/platform", data);
export const changeAdminPassword = (data) => API.put("/admin/settings/password", data);

// Security
export const getSecurityLogs = (params) => API.get("/admin/security/logs", { params });
export const getActiveSessions = () => API.get("/admin/security/sessions");
export const revokeSession = (id) => API.delete(`/admin/security/sessions/${id}`);
export const getFailedLogins = () => API.get("/admin/security/failed-logins");
