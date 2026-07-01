'use client';
import Link from 'next/link';
import { ShoppingCart, Star } from 'lucide-react';
import toast from 'react-hot-toast';
import { useCartStore } from '@/store/cartStore';
import type { Product } from '@/lib/mockData';

export default function ProductCard({ product }: { product: Product }) {
  const addItem = useCartStore(s => s.addItem);

  const handleAdd = (e: React.MouseEvent) => {
    e.preventDefault();
    if (!product.inStock) { toast.error('Out of stock'); return; }
    addItem({ ...product, qty: 1 });
    toast.success(`${product.name} added to cart! 🛒`);
  };

  const discount = product.mrp ? Math.round((1 - product.price / product.mrp) * 100) : 0;

  return (
    <Link href={`/products/${product.id}`}
      className="card p-4 flex flex-col gap-2 cursor-pointer hover:scale-[1.02] transition-transform group">
      <div className="bg-gray-50 rounded-xl h-28 flex items-center justify-center text-5xl relative">
        {product.emoji}
        {discount > 0 && (
          <span className="absolute top-2 left-2 badge bg-green-100 text-green-700">{discount}% off</span>
        )}
        {!product.inStock && (
          <span className="absolute inset-0 bg-white/70 flex items-center justify-center rounded-xl text-xs font-bold text-red-500">Out of Stock</span>
        )}
      </div>

      <p className="text-xs text-gray-400">{product.category}</p>
      <h3 className="text-sm font-semibold text-gray-900 leading-tight line-clamp-2">{product.name}</h3>

      <div className="flex items-center gap-1">
        <Star className="w-3 h-3 fill-yellow-400 text-yellow-400" />
        <span className="text-xs text-gray-500">{product.rating}</span>
      </div>

      <div className="flex items-center gap-2 mt-auto">
        <span className="font-bold text-gray-900">₹{product.price}</span>
        {product.mrp && <span className="text-xs text-gray-400 line-through">₹{product.mrp}</span>}
      </div>

      <button
        onClick={handleAdd}
        disabled={!product.inStock}
        className="btn-primary py-2 text-xs flex items-center justify-center gap-1 mt-1 disabled:opacity-50 disabled:cursor-not-allowed">
        <ShoppingCart className="w-3 h-3" /> Add to Cart
      </button>
    </Link>
  );
}
