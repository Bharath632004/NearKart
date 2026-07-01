'use client';
import Link from 'next/link';

const CATS = [
  { label: 'Dairy',      emoji: '🥛', color: 'bg-blue-50   text-blue-700'   },
  { label: 'Vegetables', emoji: '🥦', color: 'bg-green-50  text-green-700'  },
  { label: 'Snacks',     emoji: '🍪', color: 'bg-yellow-50 text-yellow-700' },
  { label: 'Beverages',  emoji: '🍵', color: 'bg-orange-50 text-orange-700' },
  { label: 'Grains',     emoji: '🍚', color: 'bg-amber-50  text-amber-700'  },
  { label: 'Bakery',     emoji: '🍞', color: 'bg-pink-50   text-pink-700'   },
  { label: 'Personal',   emoji: '🧴', color: 'bg-purple-50 text-purple-700' },
  { label: 'Household',  emoji: '🧺', color: 'bg-teal-50   text-teal-700'   },
];

export default function CategoryGrid() {
  return (
    <section className="max-w-7xl mx-auto px-4 py-12">
      <h2 className="section-title">Shop by Category</h2>
      <p className="section-subtitle">Everything you need, just a tap away</p>
      <div className="grid grid-cols-4 md:grid-cols-8 gap-3">
        {CATS.map(c => (
          <Link key={c.label} href={`/products?category=${c.label}`}
            className={`card p-4 flex flex-col items-center gap-2 cursor-pointer hover:scale-105 transition-transform ${c.color}`}>
            <span className="text-3xl">{c.emoji}</span>
            <span className="text-xs font-semibold text-center">{c.label}</span>
          </Link>
        ))}
      </div>
    </section>
  );
}
