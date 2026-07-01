import Link from 'next/link';
import { ShoppingCart } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-nearkart-dark text-gray-300 mt-16">
      <div className="max-w-7xl mx-auto px-4 py-12 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-8">
        <div>
          <div className="flex items-center gap-2 mb-3">
            <div className="w-8 h-8 bg-primary-500 rounded-lg flex items-center justify-center">
              <ShoppingCart className="w-4 h-4 text-white" />
            </div>
            <span className="text-white font-bold text-lg">NearKart</span>
          </div>
          <p className="text-sm leading-relaxed">Your neighbourhood, delivered in minutes. Fresh, local, fast.</p>
        </div>

        <div>
          <h4 className="text-white font-semibold mb-3">Quick Links</h4>
          <ul className="space-y-2 text-sm">
            {[['/', 'Home'], ['/products', 'Products'], ['/login', 'Sign In'], ['/register', 'Sign Up']].map(([href, label]) => (
              <li key={href}><Link href={href} className="hover:text-primary-400 transition">{label}</Link></li>
            ))}
          </ul>
        </div>

        <div>
          <h4 className="text-white font-semibold mb-3">Categories</h4>
          <ul className="space-y-2 text-sm">
            {['Dairy', 'Vegetables', 'Snacks', 'Beverages', 'Household'].map(c => (
              <li key={c}><Link href={`/products?category=${c}`} className="hover:text-primary-400 transition">{c}</Link></li>
            ))}
          </ul>
        </div>

        <div>
          <h4 className="text-white font-semibold mb-3">Contact</h4>
          <ul className="space-y-2 text-sm">
            <li>📧 support@nearkart.in</li>
            <li>📞 1800-123-4567 (toll free)</li>
            <li>📍 Hyderabad, Telangana</li>
          </ul>
        </div>
      </div>
      <div className="border-t border-gray-700 text-center py-4 text-xs text-gray-500">
        © {new Date().getFullYear()} NearKart. All rights reserved.
      </div>
    </footer>
  );
}
