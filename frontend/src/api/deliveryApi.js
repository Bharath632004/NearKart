import axiosInstance from './axiosInstance';

export const getDeliveryProfileApi = () => axiosInstance.get('/delivery/profile');
export const updateDeliveryProfileApi = (data) => axiosInstance.put('/delivery/profile', data);
export const getDeliveryStatsApi = () => axiosInstance.get('/delivery/stats');
