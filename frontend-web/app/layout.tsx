import type { Metadata } from 'next';
import './globals.css';
import { Toaster } from 'react-hot-toast';

export const metadata: Metadata = {
  title: 'NearKart – Hyperlocal Quick Commerce',
  description: 'Order from nearby Kirana stores. Ultra-fast delivery in 10–30 minutes.',
  keywords: 'hyperlocal, quick commerce, kirana, delivery, nearkart',
  openGraph: {
    title: 'NearKart',
    description: 'Your neighbourhood store, delivered in minutes.',
    type: 'website',
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        {children}
        <Toaster
          position="top-right"
          toastOptions={{
            style: { borderRadius: '12px', background: '#1a1a2e', color: '#fff' },
            success: { iconTheme: { primary: '#f97316', secondary: '#fff' } },
          }}
        />
      </body>
    </html>
  );
}
