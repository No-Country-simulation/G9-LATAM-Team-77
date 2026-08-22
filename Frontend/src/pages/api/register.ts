import { API_URL } from '../../lib/api';
import type { APIRoute } from 'astro';

export const POST: APIRoute = async ({ request }) => {
  try {
    const body = await request.json();
    const { email, password } = body;

    if (!email || !password) {
      return new Response(JSON.stringify({ error: "Email and password are required" }), {
        status: 400,
        headers: { 'Content-Type': 'application/json' }
      });
    }

    const res = await fetch(`${API_URL}/api/v1/auth/register`, {
      method: "POST",
      body: JSON.stringify(body),
      headers: { "Content-Type": "application/json" }
    });

    if (!res.ok) {
      const errorData = await res.json().catch(() => ({}));
      return new Response(JSON.stringify({ error: errorData.message || "Registration failed" }), {
        status: res.status,
        headers: { 'Content-Type': 'application/json' }
      });
    }

    const data = await res.json();

    return new Response(JSON.stringify({ success: true, data }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    });
  } catch {
    return new Response(JSON.stringify({ error: 'No fue posible completar el registro' }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' }
    });
  }
};
