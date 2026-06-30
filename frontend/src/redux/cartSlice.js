import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getCartApi, addToCartApi, updateCartItemApi, removeFromCartApi, clearCartApi, checkoutApi } from '../api/cartApi';

export const fetchCart = createAsyncThunk('cart/fetch', async (_, { rejectWithValue }) => {
  try { const res = await getCartApi(); return res.data; }
  catch (err) { return rejectWithValue(err.response?.data?.message || 'Failed'); }
});

export const addToCart = createAsyncThunk('cart/add', async (data, { rejectWithValue }) => {
  try { const res = await addToCartApi(data); return res.data; }
  catch (err) { return rejectWithValue(err.response?.data?.message || 'Failed'); }
});

export const updateCartItem = createAsyncThunk('cart/update', async ({ id, data }, { rejectWithValue }) => {
  try { const res = await updateCartItemApi(id, data); return res.data; }
  catch (err) { return rejectWithValue(err.response?.data?.message || 'Failed'); }
});

export const removeFromCart = createAsyncThunk('cart/remove', async (id, { rejectWithValue }) => {
  try { await removeFromCartApi(id); return id; }
  catch (err) { return rejectWithValue(err.response?.data?.message || 'Failed'); }
});

export const clearCart = createAsyncThunk('cart/clear', async (_, { rejectWithValue }) => {
  try { await clearCartApi(); return []; }
  catch (err) { return rejectWithValue(err.response?.data?.message || 'Failed'); }
});

export const checkout = createAsyncThunk('cart/checkout', async (data, { rejectWithValue }) => {
  try { const res = await checkoutApi(data); return res.data; }
  catch (err) { return rejectWithValue(err.response?.data?.message || 'Failed'); }
});

const cartSlice = createSlice({
  name: 'cart',
  initialState: { items: [], loading: false, error: null, checkoutSuccess: false },
  reducers: {
    resetCheckout(state) { state.checkoutSuccess = false; },
    clearCartError(state) { state.error = null; },
  },
  extraReducers: (builder) => {
    const setPending = (s) => { s.loading = true; s.error = null; };
    const setError = (s, a) => { s.loading = false; s.error = a.payload; };
    builder
      .addCase(fetchCart.pending, setPending)
      .addCase(fetchCart.fulfilled, (s, a) => { s.loading = false; s.items = a.payload; })
      .addCase(fetchCart.rejected, setError)
      .addCase(addToCart.pending, setPending)
      .addCase(addToCart.fulfilled, (s, a) => { s.loading = false; s.items = a.payload; })
      .addCase(addToCart.rejected, setError)
      .addCase(updateCartItem.fulfilled, (s, a) => { s.items = a.payload; })
      .addCase(removeFromCart.fulfilled, (s, a) => { s.items = s.items.filter(i => i.id !== a.payload); })
      .addCase(clearCart.fulfilled, (s) => { s.items = []; })
      .addCase(checkout.pending, setPending)
      .addCase(checkout.fulfilled, (s) => { s.loading = false; s.items = []; s.checkoutSuccess = true; })
      .addCase(checkout.rejected, setError);
  },
});

export const { resetCheckout, clearCartError } = cartSlice.actions;
export default cartSlice.reducer;
