// En el navegador, usar cadena vacía '' para que las peticiones vayan directo
// al dominio actual (ej: https://financeai-app.duckdns.org/api/v1/...) a través de Caddy.
// En el servidor (Node SSR / Docker), apuntar directo al contenedor del backend.
export const API_URL = typeof window !== 'undefined'
    ? ''
    : (process.env.INTERNAL_BACKEND_URL || 'http://backend:8080');