// En el frontend del navegador y en las vistas Astro (define:vars), siempre usar ''
// para que el navegador haga peticiones relativas a la misma URL de origen (HTTPS vía Caddy).
export const API_URL = '';

// En endpoints SSR del servidor (Node.js dentro de Docker), conectar directo al backend.
export const SERVER_API_URL = process.env.INTERNAL_BACKEND_URL || 'http://backend:8080';