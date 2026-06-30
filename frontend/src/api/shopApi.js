import axiosInstance from './axiosInstance';

export const getShopsApi = (params) => axiosInstance.get('/shops', { params });
export const getShopByIdApi = (id) => axiosInstance.get(`/shops/${id}`);
export const createShopApi = (data) => axiosInstance.post('/shops', data);
export const updateShopApi = (id, data) => axiosInstance.put(`/shops/${id}`, data);
export const deleteShopApi = (id) => axiosInstance.delete(`/shops/${id}`);
