import type { APIRoute } from 'astro';

export const ALL: APIRoute = async ({ cookies, redirect }) => {
    // Lista completa de nombres de cookies empleadas por Auth.js / NextAuth / auth-astro
    const sessionCookieNames = [
        'authjs.session-token',
        '__Secure-authjs.session-token',
        'authjs.csrf-token',
        '__Host-authjs.csrf-token',
        'authjs.callback-url',
        '__Secure-authjs.callback-url',
        'authjs.state',
        'authjs.pkce.code_verifier',
        'financeai_token',
        'financeai_session'
    ];

    sessionCookieNames.forEach((cookieName) => {
        cookies.delete(cookieName, {
            path: '/',
            httpOnly: true,
            secure: true,
            sameSite: 'lax'
        });
        cookies.delete(cookieName, {
            path: '/'
        });
    });

    return redirect('/login', 302);
};
