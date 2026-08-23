import { defineMiddleware } from 'astro:middleware';

export const onRequest = defineMiddleware(async (context, next) => {
    const forwardedProto = context.request.headers.get('x-forwarded-proto');
    const forwardedHost = context.request.headers.get('x-forwarded-host') || context.request.headers.get('host');

    // Cuando la aplicación está detrás de un proxy SSL (Caddy),
    // reescribir la URL de la Request a HTTPS para que Auth.js y Astro
    // validen correctamente el origen y no disparen errores 403 CSRF Cross-Site.
    if ((forwardedProto === 'https' || process.env.NODE_ENV === 'production') && context.request.url.startsWith('http://')) {
        const httpsUrl = context.request.url.replace(/^http:\/\/[^/]+/, `https://${forwardedHost}`);
        try {
            const newRequest = new Request(httpsUrl, {
                method: context.request.method,
                headers: context.request.headers,
                body: context.request.method !== 'GET' && context.request.method !== 'HEAD' ? context.request.body : undefined,
                // @ts-ignore
                duplex: 'half'
            });
            context.request = newRequest;
        } catch (_) {}
    }

    return next();
});
