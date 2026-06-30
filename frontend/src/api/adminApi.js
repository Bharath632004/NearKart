import axiosInstance from './axiosInstance';

export const getAllUsersApi = () => axiosInstance.get('/admin/users');
export const updateUserStatusApi = (id, status) => axiosInstance.put(`/admin/users/${id}/status`, { status });
export const deleteUserApi = (id) => axiosInstance.delete(`/admin/users/${id}`);

export const getAllMerchantsApi = () => axiosInstance.get('/admin/merchants');
export const approveMerchantApi = (id) => axiosInstance.put(`/admin/merchants/${id}/approve`);
export const rejectMerchantApi = (id) => axiosInstance.put(`/admin/merchants/${id}/reject`);

export const getAllDeliveryPartnersApi = () => axiosInstance.get('/admin/delivery');
export const approveDeliveryPartnerApi = (id) => axiosInstance.put(`/admin/delivery/${id}/approve`);

export const getAdminReportsApi = () => axiosInstance.get('/admin/reports');
