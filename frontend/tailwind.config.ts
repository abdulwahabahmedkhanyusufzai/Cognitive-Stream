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
          dark: '#05070A',
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
        'sleek-dark': 'linear-gradient(to bottom, #05070A, #0F172A)',
      },
      backdropBlur: {
        xs: '2px',
      }
    },
  },
  plugins: [
    forms,
    typography,
  ],
} satisfies Config;