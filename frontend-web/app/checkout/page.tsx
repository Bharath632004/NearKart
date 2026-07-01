'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { useCartStore } from '@/store/cartStore';
import { formatCurrency } from '@/lib/utils';
import { MapPin, CreditCard, Truck } from 'lucide-react';
import toast from 'react-hot-toast';

export default function CheckoutPage() {
  const router = useRouter();
  const { items, totalPrice, clearCart } = useCartStore();
  const [form, setForm] = useState({ name: '', phone: '', address: '', pincode: '' });
  const [paymentMethod, setPaymentMethod] = useState<'cod' | 'upi' | 'card'>('cod');
  const [loading, setLoading] = useState(false);

  const deliveryFee = totalPrice() > 299 ? 0 : 29;
  const grandTotal = totalPrice() + deliveryFee;

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  }

  async function handlePlaceOrder(e: React.FormEvent) {
    e.preventDefault();
    if (items.length === 0) { toast.error('Your cart is empty!'); return; }
    setLoading(true);
    await new Promise((r) => setTimeout(r, 1500));
    clearCart();
    toast.success('🎉 Order placed successfully!');
    router.push('/orders?success=1');
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-5xl mx-auto w-full px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-8">Checkout</h1>
        <form onSubmit={handlePlaceOrder} className="grid lg:grid-cols-3 gap-6">
          {/* Left — Delivery & Payment */}
          <div className="lg:col-span-2 flex flex-col gap-6">
            {/* Delivery address */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
              <h2 className="font-semibold text-gray-800 flex items-center gap-2 mb-4">
                <MapPin size={18} className="text-green-600" /> Delivery Address
              </h2>
              <div className="grid sm:grid-cols-2 gap-4">
                <Input required label="Full Name" name="name" value={form.name} onChange={handleChange} placeholder="Bharath Kumar" />
                <Input required label="Phone" name="phone" value={form.phone} onChange={handleChange} placeholder="9876543210" type="tel" />
              </div>
              <div className="mt-4">
                <label className="text-sm font-medium text-gray-700 block mb-1">Full Address</label>
                <textarea
                  required
                  name="address"
                  value={form.address}
                  onChange={handleChange}
                  rows={3}
                  placeholder="House / Flat, Street, Locality"
                  className="w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-300"
                />
              </div>
              <div className="mt-4 w-40">
                <Input required label="Pincode" name="pincode" value={form.pincode} onChange={handleChange} placeholder="500001" />
              </div>
            </div>

            {/* Payment method */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
              <h2 className="font-semibold text-gray-800 flex items-center gap-2 mb-4">
                <CreditCard size={18} className="text-green-600" /> Payment Method
              </h2>
              <div className="flex flex-col gap-3">
                {[{ id: 'cod', label: '💵 Cash on Delivery' }, { id: 'upi', label: '📱 UPI / QR Code' }, { id: 'card', label: '💳 Debit / Credit Card' }].map((m) => (
                  <label key={m.id} className={`flex items-center gap-3 p-3 rounded-xl border cursor-pointer transition ${
                    paymentMethod === m.id ? 'border-green-500 bg-green-50' : 'border-gray-200 hover:border-green-300'
                  }`}>
                    <input type="radio" name="payment" value={m.id} checked={paymentMethod === (m.id as 'cod'|'upi'|'card')} onChange={() => setPaymentMethod(m.id as 'cod'|'upi'|'card')} className="accent-green-600" />
                    <span className="text-sm font-medium text-gray-700">{m.label}</span>
                  </label>
                ))}
              </div>
            </div>
          </div>

          {/* Right — Order Summary */}
          <div className="flex flex-col gap-4">
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 sticky top-4">
              <h2 className="font-semibold text-gray-800 flex items-center gap-2 mb-4">
                <Truck size={18} className="text-green-600" /> Order Summary
              </h2>
              <div className="flex flex-col gap-2 text-sm text-gray-700 mb-4">
                {items.map((item) => (
                  <div key={item.id} className="flex justify-between">
                    <span>{item.emoji} {item.name} × {item.qty}</span>
                    <span className="font-medium">{formatCurrency(item.price * item.qty)}</span>
                  </div>
                ))}
              </div>
              <div className="border-t pt-4 flex flex-col gap-1 text-sm">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal</span><span>{formatCurrency(totalPrice())}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Delivery</span>
                  <span className={deliveryFee === 0 ? 'text-green-600 font-medium' : ''}>
                    {deliveryFee === 0 ? 'FREE' : formatCurrency(deliveryFee)}
                  </span>
                </div>
                <div className="flex justify-between font-bold text-gray-900 text-base mt-2">
                  <span>Total</span><span>{formatCurrency(grandTotal)}</span>
                </div>
              </div>
              <Button type="submit" loading={loading} className="w-full mt-6" size="lg">
                Place Order
              </Button>
              {totalPrice() < 299 && (
                <p className="text-xs text-center text-gray-400 mt-2">
                  Add {formatCurrency(299 - totalPrice())} more for free delivery
                </p>
              )}
            </div>
          </div>
        </form>
      </main>
      <Footer />
    </div>
  );
}
