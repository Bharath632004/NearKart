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
  const params = useParams();
  const id = Number(Array.isArray(params.id) ? params.id[0] : params.id);
  const product = MOCK_PRODUCTS.find(p => p.id === id) ?? MOCK_PRODUCTS[0];

  const [qty, setQty] = useState(1);
  const [wish, setWish] = useState(false);
  const addToCart = useCartStore(s => s.addItem);

  const handleAdd = () => {
    if (!product.inStock) { toast.error('This product is out of stock'); return; }
    addToCart({ ...product, qty });
    toast.success(`${product.name} added to cart! 🛒`);
  };

  const discount = product.mrp ? Math.round((1 - product.price / product.mrp) * 100) : 0;

  return (
    <main className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-6xl mx-auto px-4 py-8">
        <nav className="text-sm text-gray-400 mb-6 flex items-center gap-1">
          <Link href="/" className="hover:text-primary-500">Home</Link>
          <span>/</span>
          <Link href="/products" className="hover:text-primary-500">Products</Link>
          <span>/</span>
          <span className="text-gray-700">{product.name}</span>
        </nav>

        <div className="grid md:grid-cols-2 gap-10">
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

          <div>
            <div className="badge bg-orange-100 text-orange-600 mb-3">{product.category}</div>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">{product.name}</h1>
            <div className="flex items-center gap-2 mb-4">
              <div className="flex">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className={`w-4 h-4 ${ i < Math.floor(product.rating) ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'}`} />
                ))}
              </div>
              <span className="text-sm text-gray-500">{product.rating} ({product.reviews} reviews)</span>
            </div>

            <div className="flex items-baseline gap-3 mb-4">
              <span className="text-3xl font-bold text-primary-500">₹{product.price}</span>
              {product.mrp && <span className="text-lg text-gray-400 line-through">₹{product.mrp}</span>}
              {discount > 0 && <span className="badge bg-green-100 text-green-700">{discount}% off</span>}
            </div>

            <p className="text-gray-600 text-sm mb-6">{product.description}</p>

            {!product.inStock && (
              <p className="text-red-500 text-sm font-semibold mb-4">⚠️ Currently out of stock</p>
            )}

            <div className="flex items-center gap-4 mb-6">
              <span className="text-sm font-medium text-gray-700">Quantity</span>
              <div className="flex items-center gap-3 bg-gray-100 rounded-xl px-3 py-1">
                <button onClick={() => setQty(q => Math.max(1, q - 1))} className="text-gray-500 hover:text-primary-500 transition">
                  <Minus className="w-4 h-4" />
                </button>
                <span className="w-6 text-center font-bold">{qty}</span>
                <button onClick={() => setQty(q => q + 1)} className="text-gray-500 hover:text-primary-500 transition">
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            </div>

            <div className="flex gap-3 mb-6">
              <button onClick={handleAdd} disabled={!product.inStock}
                className="btn-primary flex-1 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed">
                <ShoppingCart className="w-4 h-4" /> Add to Cart
              </button>
              <button onClick={() => setWish(!wish)}
                className={`w-12 h-12 flex items-center justify-center rounded-xl border-2 transition ${
                  wish ? 'border-red-400 bg-red-50 text-red-500' : 'border-gray-200 text-gray-400 hover:border-red-300'
                }`}>
                <Heart className={`w-5 h-5 ${wish ? 'fill-red-500' : ''}`} />
              </button>
            </div>

            <div className="grid grid-cols-3 gap-3">
              {[
                { icon: <Truck className="w-4 h-4" />,      label: 'Fast Delivery', sub: '10–30 min'    },
                { icon: <Shield className="w-4 h-4" />,    label: 'Safe & Secure', sub: '100% genuine'  },
                { icon: <RotateCcw className="w-4 h-4" />, label: 'Easy Return',   sub: '24 hr window'  },
              ].map(item => (
                <div key={item.label} className="card p-3 text-center">
                  <div className="text-primary-500 flex justify-center mb-1">{item.icon}</div>
                  <p className="text-xs font-semibold text-gray-800">{item.label}</p>
                  <p className="text-[10px] text-gray-400">{item.sub}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </main>
  );
}
