'use client';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import HeroSection from '@/components/home/HeroSection';
import CategoryGrid from '@/components/home/CategoryGrid';
import FeaturedStores from '@/components/home/FeaturedStores';
import HowItWorks from '@/components/home/HowItWorks';
import PromoBanner from '@/components/home/PromoBanner';
import TrustBadges from '@/components/home/TrustBadges';

export default function HomePage() {
  return (
    <main className="min-h-screen bg-gray-50">
      <Navbar />
      <HeroSection />
      <TrustBadges />
      <CategoryGrid />
      <PromoBanner />
      <FeaturedStores />
      <HowItWorks />
      <Footer />
    </main>
  );
}
