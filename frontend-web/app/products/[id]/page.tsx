'use client';
import { useParams } from 'next/navigation';
import { useState } from 'react';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import toast from 'react-hot-toast';
import { ShoppingCart, Heart, Star, Minus, Plus, Truck, Shield, RotateCcw } from 'lucide-react';
import { MOCK_PRODUCTS } from '@/lib/mockData';
import { useCartStore } from '@/store/cartStore';
import Link from 'next/link';

export default function ProductDetailPage() {
  const { id } = useParams();
  const product = MOCK_PRODUCTS.find(p => p.id === Number(id)) || MOCK_PRODUCTS[0];
  const [qty, setQty] = useState(1);
  const [wish, setWish] = useState(false);
  const addToCart = useCartStore(s => s.addItem);

  const handleAdd = () => {
    addToCart({ ...product, qty });
    toast.success(`${product.name} added to cart! 🛒`);
  };

  return (
    <main className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-6xl mx-auto px-4 py-8">
        <nav className="text-sm text-gray-400 mb-6">
          <Link href="/" className="hover:text-primary-500">Home</Link> /{' '}
          <Link href="/products" className="hover:text-primary-500">Products</Link> /{' '}
          <span className="text-gray-700">{product.name}</span>
        </nav>

        <div className="grid md:grid-cols-2 gap-10">
          {/* Image */}
          <div className="card p-6">
            <div className="bg-gray-50 rounded-xl h-80 flex items-center justify-center text-8xl">
              {product.emoji}
            </div>
            <div className="flex gap-2 mt-4">
              {[product.emoji, '📷', '🖼️'].map((e, i) => (
                <div key={i} className="w-16 h-16 bg-gray-100 rounded-lg flex items-center justify-center text-2xl cursor-pointer hover:ring-2 ring-primary-400">{e}</div>
              ))}
            </div>
          </div>

          {/* Info */}
          <div>
            <div className="badge bg-orange-100 text-orange-600 mb-3">{product.category}</div>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">{product.name}</h1>
            <div className="flex items-center gap-2 mb-4">
              <div className="flex">{[...Array(5)].map((_,i)=><Star key={i} className={`w-4 h-4 ${ i < Math.floor(product.rating) ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'}`} />)}</div>
              <span className="text-sm text-gray-500">{product.rating} ({product.reviews} reviews)</span>
            </div>
            <div className="flex items-baseline gap-3 mb-4">
              <span className="text-3xl font-bold text-primary-500">₹{product.price}</span>
              {product.mrp && <span className="text-lg text-gray-400 line-through">₹{product.mrp}</span>}
              {product.mrp && <span className="badge bg-green-100 text-green-700">{Math.round((1-product.price/product.mrp)*100)}% off</span>}
            </div>
            <p className="text-gray-600 text-sm mb-6">{product.description}</p>

            {/* Qty */}
            <div className="flex items-center gap-4 mb-6">
          