import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Product } from '@/lib/mockData';

interface WishlistStore {
  items: Product[];
  addItem: (product: Product) => void;
  removeItem: (id: number) => void;
  isWishlisted: (id: number) => boolean;
  toggle: (product: Product) => void;
}

export const useWishlistStore = create<WishlistStore>()(
  persist(
    (set, get) => ({
      items: [],

      addItem: (product) =>
        set((state) => ({
          items: state.items.find((i) => i.id === product.id)
            ? state.items
            : [...state.items, product],
        })),

      removeItem: (id) =>
        set((state) => ({ items: state.items.filter((i) => i.id !== id) })),

      isWishlisted: (id) => get().items.some((i) => i.id === id),

      toggle: (product) => {
        const { isWishlisted, addItem, removeItem } = get();
        isWishlisted(product.id) ? removeItem(product.id) : addItem(product);
      },
    }),
    { name: 'nearkart-wishlist' }
  )
);
