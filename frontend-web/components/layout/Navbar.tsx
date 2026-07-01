'use client';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ShoppingCart, Search, MapPin, Menu, X } from 'lucide-react';
import { useState } from 'react';
import { useCartStore } from '@/store/cartStore';

export default function Navbar() {
  const pathname = usePathname();
  const [menuOpen, setMenuOpen] = useState(false);
  const totalItems = useCartStore(s => s.totalItems());

  const navLinks = [
    { href: '/',         label: 'Home' },
    { href: '/products', label: 'Products' },
  ];

  return (
    <nav className="sticky top-0 z-50 bg-white border-b border-gray-100 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between gap-4">
        <Link href="/" className="flex items-center gap-2 shrink-0">
          <div className="w-8 h-8 bg-primary-500 rounded-lg flex items-center justify-center">
            <ShoppingCart className="w-4 h-4 text-white" />
          </div>
          <span className="text-xl font-bold text-gray-900">Near<span className="text-primary-500">Kart</span></span>
        </Link>

        <button className="hidden sm:flex items-center gap-1 text-sm text-gray-500 bg-gray-50 border border-gray-200 rounded-full px-3 py-1.5 hover:border-primary-400 transition">
          <MapPin className="w-3.5 h-3.5 text-primary-500" />
          <span>Hyderabad</span>
        </button>

        <div className="hidden md:flex items-center gap-6">
          {navLinks.map(l => (
            <Link key={l.href} href={l.href}
              className={`text-sm font-medium transition ${
                pathname === l.href ? 'text-primary-500' : 'text-gray-600 hover:text-primary-500'
              }`}>
              {l.label}
            </Link>
          ))}
        </div>

        <div className="flex items-center gap-3">
          <button className="hidden sm:flex w-9 h-9 items-center justify-center rounded-xl bg-gray-50 hover:bg-gray-100 transition">
            <Search className="w-4 h-4 text-gray-500" />
          </button>

          <Link href="/cart" className="relative w-9 h-9 flex items-center justify-center rounded-xl bg-gray-50 hover:bg-gray-100 transition">
            <ShoppingCart className="w-4 h-4 text-gray-700" />
            {totalItems > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 bg-primary-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
                {totalItems > 9 ? '9+' : totalItems}
              </span>
            )}
          </Link>

          <Link href="/login" className="hidden sm:block btn-primary py-2 px-4 text-sm">Sign In</Link>

          <button className="md:hidden w-9 h-9 flex items-center justify-center" onClick={() => setMenuOpen(!menuOpen)}>
            {menuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {menuOpen && (
        <div className="md:hidden bg-white border-t border-gray-100 px-4 py-3 flex flex-col gap-3">
          {navLinks.map(l => (
            <Link key={l.href} href={l.href} onClick={() => setMenuOpen(false)}
              className={`text-sm font-medium ${ pathname === l.href ? 'text-primary-500' : 'text-gray-700' }`}>
              {l.label}
            </Link>
          ))}
          <Link href="/login" onClick={() => setMenuOpen(false)} className="btn-primary text-center py-2 text-sm">Sign In</Link>
        </div>
      )}
    </nav>
  );
}
