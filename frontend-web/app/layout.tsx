import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import Providers from '@/components/layout/Providers';

const inter = Inter({ subsets: ['latin'], display: 'swap' });

export const metadata: Metadata = {
  title: 'NearKart — Hyperlocal Grocery Delivery',
  description: 'Order fresh groceries from stores near you. Delivered in minutes.',
  keywords: ['grocery', 'delivery', 'hyperlocal', 'India', 'NearKart'],
  openGraph: {
    title: 'NearKart',
    description: 'Fresh groceries delivered from your nearest store',
    type: 'website',
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
