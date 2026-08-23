import type { APIRoute } from 'astro';

const BACKEND_INTERNAL_URL = process.env.INTERNAL_BACKEND_URL || 'http://127.0.0.1:8080';

export const ALL: APIRoute = async ({ request, params }) => {
  const url = new URL(request.url);
  const path = params.path || '';
  const targetUrl = `${BACKEND_INTERNAL_URL}/api/v1/${path}${url.search}`;

  const headers = new Headers(request.headers);
  headers.delete('host');

  try {
    const hasBody = !['GET', 'HEAD'].includes(request.method);
    const body = hasBody ? await request.arrayBuffer() : undefined;

    const response = await fetch(targetUrl, {
      method: request.method,
      headers: headers,
      body: body,
      redirect: 'manual'
    });

    const responseHeaders = new Headers(response.headers);
    responseHeaders.delete('content-encoding');

    return new Response(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: responseHeaders,
    });
  } catch (err: any) {
    console.error('Error forwarding request to backend:', err);
    return new Response(
      JSON.stringify({
        error: 'Backend Connection Error',
        message: 'No se pudo conectar con el servidor Backend (Spring Boot en puerto 8080). Asegúrate de que el Backend esté activo.'
      }),
      {
        status: 502,
        headers: { 'Content-Type': 'application/json' }
      }
    );
  }
};
