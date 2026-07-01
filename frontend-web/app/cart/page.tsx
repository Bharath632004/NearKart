'use client';
import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import { useCartStore } from '@/store/cartStore';
import { Trash2, Plus, Minus, ShoppingBag, ArrowRight } from 'lucide-react';
import toast from 'react-hot-toast';

export default function CartPage() {
  const router = useRouter();
  const { items, removeItem, updateQty, clearCart, totalItems, totalPrice } = useCartStore();
  const [promoCode, setPromoCode] = useState('');
  const [discount, setDiscount] = useState(0);

  const applyPromo = () => {
    if (promoCode.trim().toUpperCase() === 'NEARKART20') {
      setDiscount(20);
      toast.success('Promo code applied! 20% off 🎉');
    } else {
      toast.error('Invalid promo code');
    }
  };

  const discountAmount = Math.round(totalPrice() * discount / 100);
  const finalPrice = totalPrice() - discountAmount;

  if (items.length === 0) {
    return (
      <main className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-2xl mx-auto px-4 py-20 text-center">
          <ShoppingBag className="w-20 h-20 text-gray-300 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-gray-800 mb-2">Your cart is empty</h2>
          <p className="text-gray-400 mb-8">Add some items to get started!</p>
          <Link href="/products" className="btn-primary inline-flex items-center gap-2">
            Browse Products <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
        <Footer />
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-bold text-gray-900">
            Your Cart <span className="text-primary-500">({totalItems()} items)</span>
          </h1>
          <button onClick={() => { clearCart(); toast.success('Cart cleared'); }}
            className="text-sm text-red-500 hover:underline flex items-center gap-1">
            <Trash2 className="w-3.5 h-3.5" /> Clear all
          </button>
        </div>

        <div className="grid md:grid-cols-3 gap-8">
          {/* Items list */}
          <div className="md:col-span-2 space-y-4">
            {items.map(item => (
              <div key={item.id} className="card p-4 flex items-center gap-4">
                <div className="w-16 h-16 bg-gray-50 rounded-xl flex items-center justify-center text-3xl shrink-0">
                  {item.emoji}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-gray-900 truncate">{item.name}</p>
                  <p className="text-xs text-gray-400">{item.category}</p>
                  <p className="text-primary-500 font-bold mt-1">₹{item.price}</p>
                </div>
                {/* Qty controls */}
                <div className="flex items-center gap-2 bg-gray-100 rounded-xl px-2 py-1">
                  <button onClick={() => updateQty(item.id, item.qty - 1)}
                    className="text-gray-500 hover:text-primary-500 transition">
                    <Minus className="w-4 h-4" />
                  </button>
                  <span className="w-6 text-center font-bold text-sm">{item.qty}</span>
                  <button onClick={() => updateQty(item.id, item.qty + 1)}
                    className="text-gray-500 hover:text-primary-500 transition">
                    <Plus className="w-4 h-4" />
                  </button>
                </div>
                <div className="text-right shrink-0">
                  <p className="font-bold text-gray-900">₹{item.price * item.qty}</p>
                  <button onClick={() => { removeItem(item.id); toast.success('Item removed'); }}
                    className="text-red-400 hover:text-red-600 transition mt-1">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Order summary */}
          <div className="md:col-span-1">
            <div className="card p-6 sticky top-20">
              <h2 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h2>

              {/* Promo code */}
              <div className="flex gap-2 mb-4">
                <input
                  value={promoCode}
                  onChange={e => setPromoCode(e.target.value)}
                  className="input-field py-2 text-sm"
                  placeholder="Promo code"
                />
                <button onClick={applyPromo} className="btn-outline py-2 px-3 text-sm shrink-0">
                  Apply
                </button>
              </div>

              <div className="space-y-2 text-sm mb-4">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal ({totalItems()} items)</span>
                  <span>₹{totalPrice()}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Delivery fee</span>
                  <span className="text-green-600 font-medium">FREE</span>
                </div>
                {discount > 0 && (
                  <div className="flex justify-between text-green-600 font-medium">
                    <span>Promo ({discount}% off)</span>
                    <span>-₹{discountAmount}</span>
                  </div>
                )}
                <div className="border-t pt-2 flex justify-between font-bold text-gray-900 text-base">
                  <span>Total</span>
                  <span>₹{finalPrice}</span>
                </div>
              </div>

              <button
                onClick={() => { toast.success('Order placed! 🎉'); clearCart(); router.push('/'); }}
                className="btn-primary w-full flex items-center justify-center gap-2">
                Place Order <ArrowRight className="w-4 h-4" />
              </button>

              <Link href="/products" className="block text-center text-sm text-primary-500 hover:underline mt-3">
                ← Continue Shopping
              </Link>
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </main>
  );
}
