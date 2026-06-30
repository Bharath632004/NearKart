import axiosInstance from './axiosInstance';

export const getCustomerOrdersApi = () => axiosInstance.get('/customer/orders');
export const getMerchantOrdersApi = () => axiosInstance.get('/merchant/orders');
export const updateOrderStatusApi = (id, status) => axiosInstance.put(`/orders/${id}/status`, { status });
export const getDeliveryOrdersApi = () => axiosInstance.get('/delivery/orders');
export const acceptDeliveryApi = (id) => axiosInstance.put(`/delivery/orders/${id}/accept`);
export const completeDeliveryApi = (id) => axiosInstance.put(`/delivery/orders/${id}/complete`);
export const getDeliveryHistoryApi = () => axiosInstance.get('/delivery/history');
