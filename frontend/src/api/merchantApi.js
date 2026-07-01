import axiosInstance from './axiosInstance';

export const getMerchantProfileApi = () => axiosInstance.get('/merchant/profile');
export const updateMerchantProfileApi = (data) => axiosInstance.put('/merchant/profile', data);
export const getMerchantDashboardStatsApi = () => axiosInstance.get('/merchant/stats');
export const getMerchantShopApi = () => axiosInstance.get('/merchant/shop');
export const updateMerchantShopApi = (data) => axiosInstance.put('/merchant/shop', data);
