import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import { MOCK_STORES } from '@/lib/mockData';
import { MapPin, Star, Clock } from 'lucide-react';
import Link from 'next/link';

export default function StoresPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-5xl mx-auto w-full px-4 py-10">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Stores Near You</h1>
        <p className="text-gray-500 mb-8">Hyperlocal stores delivering to your doorstep</p>

        <div className="grid sm:grid-cols-2 lg:grid-cols-2 gap-6">
          {MOCK_STORES.map((store) => (
            <div
              key={store.id}
              className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition"
            >
              <div className="flex items-start gap-4">
                <div className="text-4xl">{store.emoji}</div>
                <div className="flex-1">
                  <h2 className="font-semibold text-gray-900 text-lg">{store.name}</h2>
                  <div className="flex items-center gap-1 text-gray-500 text-sm mt-1">
                    <MapPin size={13} />
                    <span>{store.area}</span>
                  </div>
                  <div className="flex items-center gap-3 mt-2 text-sm">
                    <span className="flex items-center gap-1 text-yellow-600 font-medium">
                      <Star size={13} className="fill-yellow-500 text-yellow-500" />
                      {store.rating}
                    </span>
                    <span className="flex items-center gap-1 text-green-700 font-medium">
                      <Clock size={13} />
                      {store.deliveryTime}
                    </span>
                  </div>
                  <div className="flex flex-wrap gap-1.5 mt-3">
                    {store.categories.map((c) => (
                      <span key={c} className="bg-green-50 text-green-700 text-xs px-2 py-0.5 rounded-full">{c}</span>
                    ))}
                  </div>
                </div>
              </div>
              <Link
                href="/products"
                className="mt-4 block w-full text-center bg-green-600 hover:bg-green-700 text-white text-sm font-medium py-2 rounded-lg transition"
              >
                Shop Now
              </Link>
            </div>
          ))}
        </div>
      </main>
      <Footer />
    </div>
  );
}
