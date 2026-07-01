import { Star, Clock } from 'lucide-react';
import { MOCK_STORES } from '@/lib/mockData';

export default function FeaturedStores() {
  return (
    <section className="max-w-7xl mx-auto px-4 py-12">
      <h2 className="section-title">Stores Near You</h2>
      <p className="section-subtitle">Top-rated local kirana stores ready to deliver</p>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        {MOCK_STORES.map(store => (
          <div key={store.id} className="card p-5 cursor-pointer hover:scale-[1.02] transition-transform">
            <div className="text-4xl mb-3">{store.emoji}</div>
            <h3 className="font-bold text-gray-900 text-sm mb-1">{store.name}</h3>
            <p className="text-xs text-gray-400 mb-3">📍 {store.area}</p>
            <div className="flex items-center justify-between text-xs">
              <span className="flex items-center gap-1 text-yellow-600 font-medium">
                <Star className="w-3 h-3 fill-yellow-400 text-yellow-400" /> {store.rating}
              </span>
              <span className="flex items-center gap-1 text-green-600 font-medium">
                <Clock className="w-3 h-3" /> {store.deliveryTime}
              </span>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
