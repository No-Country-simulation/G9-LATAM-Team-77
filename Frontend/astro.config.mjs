// @ts-check
import { defineConfig } from 'astro/config';
import tailwindcss from '@tailwindcss/vite';
import auth from 'auth-astro';
import node from '@astrojs/node';

// https://astro.build/config
export default defineConfig({
  output: 'server',
  adapter: node({
    mode: 'standalone'
  }),
  server: {
    host: true
  },
  vite: {
    envDir: '../',
    build: {
      sourcemap: false
    },
    server: {
      allowedHosts: true,
      proxy: {
        '/api/v1': {
          target: 'http://127.0.0.1:8080',
          changeOrigin: true
        }
      }
    },
    plugins: [tailwindcss()]
  },
  integrations: [auth()]
});