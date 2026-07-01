'use client';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import ProductCard from '@/components/products/ProductCard';
import EmptyState from '@/components/ui/EmptyState';
import { useWishlistStore } from '@/store/wishlistStore';

export default function WishlistPage() {
  const { items } = useWishlistStore();

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-6xl mx-auto w-full px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">My Wishlist</h1>
        {items.length === 0 ? (
          <EmptyState
            emoji="❤️"
            title="Your wishlist is empty"
            description="Save products you love and find them here anytime."
            action={{ label: 'Explore Products', href: '/products' }}
          />
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
            {items.map((p) => <ProductCard key={p.id} product={p} />)}
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
}
