/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f4f7ff',
          100: '#e5ecff',
          200: '#c9d6ff',
          300: '#a8b9ff',
          400: '#8492ff',
          500: '#6470ff',
          600: '#4f54f0',
          700: '#4041c6',
          800: '#33379d',
          900: '#2a2c80',
        },
      },
      boxShadow: {
        soft: '0 20px 60px rgba(15, 23, 42, 0.08)',
      },
      backgroundImage: {
        hero: 'radial-gradient(circle at top left, rgba(100,112,255,0.16), transparent 35%), radial-gradient(circle at top right, rgba(15,23,42,0.08), transparent 30%)',
      },
    },
  },
  plugins: [],
};
