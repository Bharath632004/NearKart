'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Search, MapPin, Zap } from 'lucide-react';

export default function HeroSection() {
  const [query, setQuery] = useState('');
  const router = useRouter();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) router.push(`/products?q=${encodeURIComponent(query.trim())}`);
  };

  return (
    <section className="bg-gradient-to-br from-orange-50 via-orange-100 to-amber-50 py-16 px-4">
      <div className="max-w-3xl mx-auto text-center">
        <div className="inline-flex items-center gap-2 bg-orange-100 border border-orange-200 text-orange-700 text-xs font-semibold px-3 py-1 rounded-full mb-4">
          <Zap className="w-3 h-3" /> 10–30 min delivery
        </div>
        <h1 className="text-4xl md:text-5xl font-extrabold text-gray-900 leading-tight mb-4">
          Your neighbourhood,{' '}<span className="text-primary-500">delivered fast 🛵</span>
        </h1>
        <p className="text-gray-500 text-lg mb-8">Order groceries, snacks & daily essentials from local kirana stores near you.</p>

        <form onSubmit={handleSearch} className="flex gap-3 max-w-xl mx-auto">
          <div className="flex-1 relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              value={query}
              onChange={e => setQuery(e.target.value)}
              className="input-field pl-12 py-4 text-base shadow-md"
              placeholder="Search milk, bread, rice..."
            />
          </div>
          <button type="submit" className="btn-primary shrink-0">Search</button>
        </form>

        <div className="flex items-center justify-center gap-2 mt-4 text-sm text-gray-400">
          <MapPin className="w-4 h-4 text-primary-500" />
          <span>Delivering to <span className="text-gray-700 font-medium">Hyderabad</span></span>
        </div>
      </div>
    </section>
  );
}
