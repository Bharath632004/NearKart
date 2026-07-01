'use client';
import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import EmptyState from '@/components/ui/EmptyState';
import Badge from '@/components/ui/Badge';
import { formatCurrency } from '@/lib/utils';
import { CheckCircle2 } from 'lucide-react';
import toast from 'react-hot-toast';

interface MockOrder {
  id: string;
  date: string;
  status: 'Delivered' | 'Out for Delivery' | 'Processing';
  total: number;
  items: { name: string; qty: number; emoji: string }[];
}

const MOCK_ORDERS: MockOrder[] = [
  {
    id: 'NK10045',
    date: '28 Jun 2026',
    status: 'Delivered',
    total: 486,
    items: [{ name: 'Fresh Milk 1L', qty: 2, emoji: '🥛' }, { name: 'Parle-G Biscuits', qty: 1, emoji: '🍪' }, { name: 'Tomatoes 500g', qty: 3, emoji: '🍅' }],
  },
  {
    id: 'NK10032',
    date: '20 Jun 2026',
    status: 'Delivered',
    total: 320,
    items: [{ name: 'Basmati Rice 5kg', qty: 1, emoji: '🍚' }],
  },
];

const statusVariant: Record<string, 'green' | 'orange' | 'blue'> = {
  Delivered: 'green',
  'Out for Delivery': 'orange',
  Processing: 'blue',
};

export default function OrdersPage() {
  const searchParams = useSearchParams();
  const [orders] = useState<MockOrder[]>(MOCK_ORDERS);

  useEffect(() => {
    if (searchParams.get('success') === '1') {
      toast.success('Your order has been placed! 🎉');
    }
  }, [searchParams]);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-3xl mx-auto w-full px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">My Orders</h1>
        {orders.length === 0 ? (
          <EmptyState
            emoji="📦"
            title="No orders yet"
            description="Your completed orders will appear here."
            action={{ label: 'Start Shopping', href: '/products' }}
          />
        ) : (
          <div className="flex flex-col gap-4">
            {orders.map((order) => (
              <div key={order.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
                <div className="flex justify-between items-start mb-3">
                  <div>
                    <p className="font-semibold text-gray-900">Order #{order.id}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{order.date}</p>
                  </div>
                  <Badge variant={statusVariant[order.status] || 'gray'}>
                    {order.status === 'Delivered' && <CheckCircle2 size={11} className="mr-1" />}
                    {order.status}
                  </Badge>
                </div>
                <div className="flex flex-wrap gap-2 mb-4">
                  {order.items.map((item, i) => (
                    <span key={i} className="text-sm text-gray-600 bg-gray-50 px-3 py-1 rounded-full">
                      {item.emoji} {item.name} × {item.qty}
                    </span>
                  ))}
                </div>
                <div className="flex justify-between items-center border-t pt-3">
                  <span className="text-sm text-gray-500">Total Paid</span>
                  <span className="font-bold text-gray-900">{formatCurrency(order.total)}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
}
