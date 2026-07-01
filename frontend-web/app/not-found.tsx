import Link from 'next/link';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';

export default function NotFound() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 flex flex-col items-center justify-center text-center px-4">
        <span className="text-8xl mb-6">🛒</span>
        <h1 className="text-5xl font-extrabold text-gray-800 mb-3">404</h1>
        <p className="text-xl text-gray-500 mb-2">Oops! Page not found.</p>
        <p className="text-gray-400 mb-8 max-w-sm">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <Link
          href="/"
          className="bg-green-600 hover:bg-green-700 text-white font-medium px-6 py-3 rounded-xl transition"
        >
          Back to Home
        </Link>
      </main>
      <Footer />
    </div>
  );
}
