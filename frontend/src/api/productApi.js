import axiosInstance from './axiosInstance';

export const getProductsByShopApi = (shopId) => axiosInstance.get(`/shops/${shopId}/products`);
export const getMerchantProductsApi = () => axiosInstance.get('/merchant/products');
export const createProductApi = (data) => axiosInstance.post('/merchant/products', data);
export const updateProductApi = (id, data) => axiosInstance.put(`/merchant/products/${id}`, data);
export const deleteProductApi = (id) => axiosInstance.delete(`/merchant/products/${id}`);
