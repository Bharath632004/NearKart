/** @type {import('next').NextConfig} */
const nextConfig = {
  images: {
    domains: ['images.unsplash.com', 'via.placeholder.com', 'localhost'],
  },
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
    NEXT_PUBLIC_GOOGLE_MAPS_KEY: process.env.NEXT_PUBLIC_GOOGLE_MAPS_KEY || '',
  },
};

module.exports = nextConfig;
