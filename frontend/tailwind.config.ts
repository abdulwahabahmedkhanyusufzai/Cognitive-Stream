import type { Config } from 'tailwindcss';
import forms from '@tailwindcss/forms';
import typography from '@tailwindcss/typography';

export default {
  content: [
    "./src/**/*.{html,ts}"
  ],
  theme: {
    extend: {
      colors: {
        nebula: {
          dark: '#020406',
          violet: '#7C3AED',
          cyan: '#06B6D4',
          blue: '#2563EB',
          deep: '#0F172A',
        },
        brand: {
          DEFAULT: '#7C3AED',
          light: '#8B5CF6',
          dark: '#6D28D9',
        }
      },
      backgroundImage: {
        'sleek-dark': 'linear-gradient(to bottom, #020406, #0F172A)',
      },
      backdropBlur: {
        xs: '2px',
      },
      animation: {
        'pulse-slow': 'pulse-slow 12s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'float': 'float 20s ease-in-out infinite',
      },
      keyframes: {
        'pulse-slow': {
          '0%, 100%': { opacity: '0.4' },
          '50%': { opacity: '1' },
        },
        'float': {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '50%': { transform: 'translate(10%, 15%) scale(1.1)' },
        }
      }
    },
  },
  plugins: [
    forms,
    typography,
  ],
} satisfies Config;