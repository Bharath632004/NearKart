'use client';
import { useRouter } from 'next/navigation';

export default function PromoBanner() {
  const router = useRouter();
  return (
    <section className="max-w-7xl mx-auto px-4 py-6">
      <div className="bg-gradient-to-r from-primary-500 to-orange-400 rounded-2xl p-8 flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="text-white">
          <p className="text-sm font-semibold opacity-80 mb-1">Limited time offer 🎉</p>
          <h2 className="text-2xl md:text-3xl font-extrabold mb-2">Get 20% off your first order!</h2>
          <p className="text-sm opacity-80">Use code <span className="font-bold bg-white/20 px-2 py-0.5 rounded-lg">NEARKART20</span> at checkout.</p>
        </div>
        <button
          onClick={() => router.push('/products')}
          className="shrink-0 bg-white text-primary-600 font-bold py-3 px-8 rounded-xl hover:bg-orange-50 transition shadow-lg">
          Shop Now →
        </button>
      </div>
    </section>
  );
}
