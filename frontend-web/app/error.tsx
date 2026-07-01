'use client';
import { useEffect } from 'react';
import Button from '@/components/ui/Button';

export default function Error({ error, reset }: { error: Error; reset: () => void }) {
  useEffect(() => { console.error(error); }, [error]);
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 text-center px-4">
      <span className="text-6xl mb-4">⚠️</span>
      <h2 className="text-2xl font-bold text-gray-800 mb-2">Something went wrong!</h2>
      <p className="text-gray-500 mb-6">{error.message || 'An unexpected error occurred.'}</p>
      <Button onClick={reset}>Try Again</Button>
    </div>
  );
}
