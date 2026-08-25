import { defineConfig } from 'astro/config';
import tailwindcss from '@tailwindcss/vite';
import auth from 'auth-astro';
import node from '@astrojs/node';
import fs from 'node:fs';
import path from 'node:path';

// Cargar variables desde el .env maestro en la raíz del proyecto hacia process.env
const rootEnvPath = path.resolve(process.cwd(), '../.env');
if (fs.existsSync(rootEnvPath)) {
  const envContent = fs.readFileSync(rootEnvPath, 'utf-8');
  envContent.split(/\r?\n/).forEach((line) => {
    const trimmed = line.trim();
    if (trimmed && !trimmed.startsWith('#')) {
      const eqIdx = trimmed.indexOf('=');
      if (eqIdx !== -1) {
        const key = trimmed.slice(0, eqIdx).trim();
        let val = trimmed.slice(eqIdx + 1).trim();
        if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) {
          val = val.slice(1, -1);
        }
        if (!process.env[key]) {
          process.env[key] = val;
        }
      }
    }
  });
}

const authSecret = process.env.AUTH_SECRET;
const siteUrl = process.env.SITE_URL || 'https://financeai-app.duckdns.org';

// https://astro.build/config
export default defineConfig({
  site: siteUrl,
  output: 'server',
  security: {
    checkOrigin: false
  },
  adapter: node({
    mode: 'standalone'
  }),
  server: {
    host: true
  },
  vite: {
    envDir: '../',
    define: {
      'import.meta.env.AUTH_TRUST_HOST': 'true',
      'import.meta.env.AUTH_SECRET': JSON.stringify(authSecret),
      'import.meta.env.AUTH_URL': JSON.stringify(siteUrl),
      'process.env.AUTH_TRUST_HOST': '"true"',
      'process.env.AUTH_URL': JSON.stringify(siteUrl),
      'process.env.NEXTAUTH_URL': JSON.stringify(siteUrl),
      'process.env.AUTH_SECRET': JSON.stringify(authSecret)
    },
    build: {
      sourcemap: false
    },
    server: {
      allowedHosts: true,
      proxy: {
        '/api/v1': {
          target: process.env.BACKEND_API_URL || 'https://financeai-app.duckdns.org',
          changeOrigin: true,
          secure: false
        }
      }
    },
    plugins: [tailwindcss()]
  },
  integrations: [auth()]
});