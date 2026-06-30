import axiosInstance from './axiosInstance';

export const getCartApi = () => axiosInstance.get('/cart');
export const addToCartApi = (data) => axiosInstance.post('/cart', data);
export const updateCartItemApi = (id, data) => axiosInstance.put(`/cart/${id}`, data);
export const removeFromCartApi = (id) => axiosInstance.delete(`/cart/${id}`);
export const clearCartApi = () => axiosInstance.delete('/cart');
export const checkoutApi = (data) => axiosInstance.post('/cart/checkout', data);
