/// <reference types="vitest" />
import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./vitest.setup.ts'],
    include: ['src/**/*.{test,spec}.{js,ts,jsx,tsx}', 'tests/**/*.{test,spec}.{js,ts,jsx,tsx}'],
  },
});
