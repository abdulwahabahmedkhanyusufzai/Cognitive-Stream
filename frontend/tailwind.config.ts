import type { Config } from 'tailwindcss';
import forms from '@tailwindcss/forms';
import typography from '@tailwindcss/typography';

export default {
  content: [
    "./src/**/*.{html,ts}"
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui'],
        heading: ['Outfit', 'sans-serif'],
      },
      colors: {
        nebula: {
          dark: '#050505',
          card: '#121212',
          surface: '#1a1a1a',
          violet: '#8B5CF6',
          cyan: '#22D3EE',
          fuchsia: '#D946EF',
          muted: '#737373',
          border: '#262626',
        }
      },
      backgroundImage: {
        'gradient-premium': 'linear-gradient(135deg, #8B5CF6 0%, #D946EF 100%)',
        'gradient-surface': 'linear-gradient(180deg, rgba(255,255,255,0.05) 0%, transparent 100%)',
        'sleek-dark': 'linear-gradient(to bottom, #050505, #121212)',
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