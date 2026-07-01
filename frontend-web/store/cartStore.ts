import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Product } from '@/lib/mockData';

export interface CartItem extends Product {
  qty: number;
}

interface CartStore {
  items: CartItem[];
  addItem: (product: Product & { qty: number }) => void;
  removeItem: (id: number) => void;
  updateQty: (id: number, qty: number) => void;
  clearCart: () => void;
  totalItems: () => number;
  totalPrice: () => number;
}

export const useCartStore = create<CartStore>()(
  persist(
    (set, get) => ({
      items: [],

      addItem: (product) => {
        const { qty, ...rest } = product;
        set(state => {
          const existing = state.items.find(i => i.id === rest.id);
          if (existing) {
            return { items: state.items.map(i => i.id === rest.id ? { ...i, qty: i.qty + qty } : i) };
          }
          return { items: [...state.items, { ...rest, qty }] };
        });
      },

      removeItem: (id) => set(state => ({ items: state.items.filter(i => i.id !== id) })),

      updateQty: (id, qty) => set(state => ({
        items: qty <= 0
          ? state.items.filter(i => i.id !== id)
          : state.items.map(i => i.id === id ? { ...i, qty } : i),
      })),

      clearCart: () => set({ items: [] }),

      totalItems: () => get().items.reduce((sum, i) => sum + i.qty, 0),

      totalPrice: () => get().items.reduce((sum, i) => sum + i.price * i.qty, 0),
    }),
    { name: 'nearkart-cart' }
  )
);
