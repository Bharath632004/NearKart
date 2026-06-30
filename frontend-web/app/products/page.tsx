'use client';
import { useState } from 'react';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import ProductCard from '@/components/products/ProductCard';
import ProductFilters from '@/components/products/ProductFilters';
import { Search, SlidersHorizontal } from 'lucide-react';
import { MOCK_PRODUCTS } from '@/lib/mockData';

export default function ProductsPage() {
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('All');
  const [sortBy, setSortBy] = useState('popular');
  const [showFilters, setShowFilters] = useState(false);

  const filtered = MOCK_PRODUCTS.filter(p =>
    (category === 'All' || p.category === category) &&
    (p.name.toLowerCase().includes(search.toLowerCase()) ||
     p.category.toLowerCase().includes(search.toLowerCase()))
  ).sort((a, b) => sortBy === 'price-asc' ? a.price - b.price : sortBy === 'price-desc' ? b.price - a.price : b.rating - a.rating);

  return (
    <main className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">All Products</h1>
        <p className="text-gray-500 mb-6">{filtered.length} products found near you</p>

        {/* Search + Sort */}
        <div className="flex gap-3 mb-6">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input value={search} onChange={e => setSearch(e.target.value)}
              className="input-field pl-10" placeholder="Search products, brands..." />
          </div>
          <select value={sortBy} onChange={e => setSortBy(e.target.value)}
            className="input-field w-auto px-4">
            <option value="popular">Most Popular</option>
            <option value="price-asc">Price: Low → High</option>
            <option value="price-desc">Price: High → Low</option>
          </select>
          <button onClick={() => setShowFilters(!showFilters)}
            className="btn-outline flex items-center gap-2">
            <SlidersHorizontal className="w-4 h-4" /> Filters
          </button>
        </div>

        <div className="flex gap-6">
          {showFilters && (
            <div className="w-64 shrink-0">
              <ProductFilters selected={category} onSelect={setCategory} />
            </div>
          )}
          <div className="flex-1">
            {filtered.length === 0 ? (
              <div className="text-center py-20">
                <p className="text-6xl mb-4">🔍</p>
                <p className="text-gray-500">No products found. Try a different search.</p>
              </div>
            ) : (
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {filtered.map(p => <ProductCard key={p.id} product={p} />)}
              </div>
            )}
          </div>
        </div>
      </div>
      <Footer />
    </main>
  );
}
