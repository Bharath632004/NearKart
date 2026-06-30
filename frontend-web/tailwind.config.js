/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50:  '#fff7ed',
          100: '#ffedd5',
          200: '#fed7aa',
          300: '#fdba74',
          400: '#fb923c',
          500: '#f97316',
          600: '#ea580c',
          700: '#c2410c',
          800: '#9a3412',
          900: '#7c2d12',
        },
        nearkart: {
          orange: '#f97316',
          dark:   '#1a1a2e',
          light:  '#fff7ed',
        },
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      animation: {
        'fade-in':    'fadeIn 0.5s ease-in-out',
        'slide-up':   'slideUp 0.4s ease-out',
        'bounce-in':  'bounceIn 0.6s ease-out',
        'pulse-slow': 'pulse 3s cubic-bezier(0.4,0,0.6,1) infinite',
      },
      keyframes: {
        fadeIn:   { '0%': { opacity: '0' },             '100%': { opacity: '1' } },
        slideUp:  { '0%': { transform: 'translateY(20px)', opacity: '0' }, '100%': { transform: 'translateY(0)', opacity: '1' } },
        bounceIn: { '0%': { transform: 'scale(0.9)', opacity: '0' }, '100%': { transform: 'scale(1)', opacity: '1' } },
      },
    },
  },
  plugins: [],
};
